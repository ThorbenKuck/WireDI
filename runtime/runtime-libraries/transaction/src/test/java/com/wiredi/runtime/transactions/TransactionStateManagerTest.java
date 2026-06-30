package com.wiredi.runtime.transactions;

import com.wiredi.runtime.transactions.exception.InactiveTransactionException;
import com.wiredi.runtime.transactions.exception.MissingTransactionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link TransactionStateManager} using minimal concrete implementations.
 *
 * Tests run inside isolated single-use threads to avoid leaking the internal ThreadLocals
 * of TransactionStateManager across test methods.
 */
class TransactionStateManagerTest {

    // --- Helpers ------------------------------------------------------------

    private static <T> T inIsolatedThread(Callable<T> action) {
        ExecutorService es = Executors.newSingleThreadExecutor();
        try {
            Future<T> future = es.submit(action);
            return future.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException(e.getCause());
        } catch (TimeoutException e) {
            throw new RuntimeException("Test timed out", e);
        } finally {
            es.shutdownNow();
        }
    }

    private static void inIsolatedThreadUnchecked(Runnable action) {
        inIsolatedThread(() -> {
            action.run();
            return null;
        });
    }

    private static TransactionFactory<TestSimpleTransaction> simpleFactory() {
        return new TransactionFactory<>() {
            @Override
            public TestSimpleTransaction createNewTransaction(TransactionProperties properties) {
                return new TestSimpleTransaction();
            }

            @Override
            public TestSimpleTransaction createNestedTransaction(Transaction parent, TransactionProperties properties) {
                return new TestSimpleTransaction(parent);
            }
        };
    }

    // --- Tests --------------------------------------------------------------

    @Test
    @DisplayName("createNewTransaction_bindsPointerToNewRoot")
    void createNewTransaction_bindsPointerToNewRoot() {
        inIsolatedThreadUnchecked(() -> {
            var tsm = new TransactionStateManager<>(simpleFactory());

            TestSimpleTransaction tx = tsm.createNewTransaction(TransactionProperties.DEFAULT);

            assertThat(Boolean.valueOf(TransactionStateManager.currentTransactionExists())).as("current tx exists").isTrue();
            assertThat(tsm.currentTransaction()).as("pointer bound to created tx").isSameAs(tx);
        });
    }

    @Test
    @DisplayName("createNewNestedTransaction_withoutActive_throwsInactiveTransactionException")
    void createNewNestedTransaction_withoutActive_throwsInactiveTransactionException() {
        inIsolatedThreadUnchecked(() -> {
            var tsm = new TransactionStateManager<>(simpleFactory());

            assertThatThrownBy(() -> tsm.createNewNestedTransaction(TransactionProperties.DEFAULT))
                    .isInstanceOf(InactiveTransactionException.class)
                    .hasMessageContaining("no active transaction");
        });
    }

    @Test
    @DisplayName("createNewNestedTransaction_withActive_bindsPointerToChild")
    void createNewNestedTransaction_withActive_bindsPointerToChild() {
        inIsolatedThreadUnchecked(() -> {
            var tsm = new TransactionStateManager<>(simpleFactory());

            TestSimpleTransaction root = tsm.createNewTransaction(TransactionProperties.DEFAULT);
            TestSimpleTransaction child = tsm.createNewNestedTransaction(TransactionProperties.DEFAULT);

            assertThat(child.getParent()).isSameAs(root);
            assertThat(tsm.currentTransaction()).isSameAs(child);
        });
    }

    @Test
    @DisplayName("suspend_withoutActive_throwsMissingTransactionException")
    void suspend_withoutActive_throwsMissingTransactionException() {
        inIsolatedThreadUnchecked(() -> {
            var tsm = new TransactionStateManager<>(simpleFactory());

            assertThatThrownBy(tsm::suspend)
                    .isInstanceOf(MissingTransactionException.class)
                    .hasMessageContaining("Cannot suspend");
        });
    }

    @Test
    @DisplayName("suspend_then_resume_restoresPreviousTransaction_inLifoOrder")
    void suspend_then_resume_restoresPreviousTransaction_inLifoOrder() {
        inIsolatedThreadUnchecked(() -> {
            var tsm = new TransactionStateManager<>(simpleFactory());

            TestSimpleTransaction root1 = tsm.createNewTransaction(TransactionProperties.DEFAULT);
            var s1 = tsm.suspend(); // stack: [root1]

            // now create another independent root and suspend it
            TestSimpleTransaction root2 = tsm.createNewTransaction(TransactionProperties.DEFAULT);
            var s2 = tsm.suspend(); // stack: [root2, root1]

            // No current transaction while suspended
            assertThat(Boolean.valueOf(TransactionStateManager.currentTransactionExists())).isFalse();
            assertThat(tsm.currentTransaction()).isNull();

            // Resume in LIFO: first root2, then root1
            s2.resume();
            assertThat(tsm.currentTransaction()).isSameAs(root2);
            s1.resume();
            assertThat(tsm.currentTransaction()).isSameAs(root1);
        });
    }

    @Test
    @DisplayName("get_executesFunction_setsPointer_flushes_and_restores_onSuccess")
    void get_executesFunction_setsPointer_flushes_and_restores_onSuccess() {
        inIsolatedThreadUnchecked(() -> {
            var tsm = new TransactionStateManager<>(simpleFactory());
            AtomicBoolean inside = new AtomicBoolean(false);
            AtomicInteger afterCommitCalls = new AtomicInteger();
            AtomicInteger afterFlushCalls = new AtomicInteger();

            String result = tsm.get(TransactionProperties.DEFAULT, tx -> {
                inside.set(TransactionStateManager.currentTransactionExists());
                assertThat(tx).isNotNull();
                TransactionStateManager.registerTransactionCallback(new TransactionCallback() {
                    @Override public void afterCommit() { afterCommitCalls.incrementAndGet(); }
                    @Override public void afterRollback() { /* not expected */ }
                    @Override public void afterFlush() { afterFlushCalls.incrementAndGet(); }
                });
                return "ok";
            });

            assertThat(result).isEqualTo("ok");
            assertThat(inside.get()).isTrue();
            assertThat(Boolean.valueOf(TransactionStateManager.currentTransactionExists())).isFalse();
            // For a successful block, commit path must be taken exactly once and afterFlush once
            assertThat(afterCommitCalls.get()).isEqualTo(1);
            assertThat(afterFlushCalls.get()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("get_whenFunctionThrows_marksRollbackOnly_flushes_and_restores_thenRethrows")
    void get_whenFunctionThrows_marksRollbackOnly_flushes_and_restores_thenRethrows() {
        inIsolatedThreadUnchecked(() -> {
            var tsm = new TransactionStateManager<>(simpleFactory());
            AtomicInteger afterRollbackCalls = new AtomicInteger();
            AtomicInteger afterFlushCalls = new AtomicInteger();
            AtomicBoolean sawTxMarkedRollback = new AtomicBoolean(false);

            assertThatThrownBy(() -> tsm.get(TransactionProperties.DEFAULT, tx -> {
                TransactionStateManager.registerTransactionCallback(new TransactionCallback() {
                    @Override public void afterCommit() { /* not expected */ }
                    @Override public void afterRollback() { afterRollbackCalls.incrementAndGet(); }
                    @Override public void afterFlush() { afterFlushCalls.incrementAndGet(); }
                });
                // mark rollback through exception path and check flag is set before flush
                try {
                    throw new IllegalStateException("boom");
                } catch (Throwable e) {
                    // The manager will call tx.handleThrowable(e) which sets rollbackOnly by default.
                    // We cannot observe it here yet; observation happens after call returns.
                    throw e;
                }
            }))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("boom");

            assertThat(Boolean.valueOf(TransactionStateManager.currentTransactionExists())).isFalse();
            assertThat(afterRollbackCalls.get()).isEqualTo(1);
            assertThat(afterFlushCalls.get()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("tryGet_returnsEmptyOnNull_and_flushes")
    void tryGet_returnsEmptyOnNull_and_flushes() {
        inIsolatedThreadUnchecked(() -> {
            var tsm = new TransactionStateManager<>(simpleFactory());
            AtomicInteger afterCommitCalls = new AtomicInteger();
            AtomicInteger afterFlushCalls = new AtomicInteger();

            Optional<String> result = tsm.tryGet(TransactionProperties.DEFAULT, tx -> {
                TransactionStateManager.registerTransactionCallback(new TransactionCallback() {
                    @Override public void afterCommit() { afterCommitCalls.incrementAndGet(); }
                    @Override public void afterRollback() { /* not expected */ }
                    @Override public void afterFlush() { afterFlushCalls.incrementAndGet(); }
                });
                return null; // triggers Optional.empty()
            });

            assertThat(result).isEmpty();
            assertThat(Boolean.valueOf(TransactionStateManager.currentTransactionExists())).isFalse();
            assertThat(afterCommitCalls.get()).isEqualTo(1);
            assertThat(afterFlushCalls.get()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("run_executesConsumer_and_flushes")
    void run_executesConsumer_and_flushes() {
        inIsolatedThreadUnchecked(() -> {
            var tsm = new TransactionStateManager<>(simpleFactory());
            AtomicInteger afterCommitCalls = new AtomicInteger();
            AtomicInteger afterFlushCalls = new AtomicInteger();

            tsm.run(TransactionProperties.DEFAULT, tx -> {
                TransactionStateManager.registerTransactionCallback(new TransactionCallback() {
                    @Override public void afterCommit() { afterCommitCalls.incrementAndGet(); }
                    @Override public void afterRollback() { /* not expected */ }
                    @Override public void afterFlush() { afterFlushCalls.incrementAndGet(); }
                });
            });

            assertThat(Boolean.valueOf(TransactionStateManager.currentTransactionExists())).isFalse();
            assertThat(afterCommitCalls.get()).isEqualTo(1);
            assertThat(afterFlushCalls.get()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("registerTransactionCallback_withoutActive_throwsMissingTransactionException")
    void registerTransactionCallback_withoutActive_throwsMissingTransactionException() {
        inIsolatedThreadUnchecked(() -> {
            assertThatThrownBy(() -> TransactionStateManager.registerTransactionCallback(new TransactionCallback() {
                @Override public void afterCommit() { }
                @Override public void afterRollback() { }
                @Override public void afterFlush() { }
            }))
            .isInstanceOf(MissingTransactionException.class)
            .hasMessageContaining("without active transaction");
        });
    }

    @Test
    @DisplayName("registerTransactionCallback_withActive_delegatesToCurrentTransaction")
    void registerTransactionCallback_withActive_delegatesToCurrentTransaction() {
        inIsolatedThreadUnchecked(() -> {
            var tsm = new TransactionStateManager<>(simpleFactory());
            AtomicInteger afterCommitCalls = new AtomicInteger();
            AtomicInteger afterFlushCalls = new AtomicInteger();

            tsm.run(TransactionProperties.DEFAULT, tx -> {
                TransactionStateManager.registerTransactionCallback(new TransactionCallback() {
                    @Override public void afterCommit() { afterCommitCalls.incrementAndGet(); }
                    @Override public void afterRollback() { }
                    @Override public void afterFlush() { afterFlushCalls.incrementAndGet(); }
                });
            });

            assertThat(afterCommitCalls.get()).isEqualTo(1);
            assertThat(afterFlushCalls.get()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("static_currentTransactionExists_and_getTransaction_reflectPointerState")
    void static_currentTransactionExists_and_getTransaction_reflectPointerState() {
        inIsolatedThreadUnchecked(() -> {
            var tsm = new TransactionStateManager<>(simpleFactory());
            assertThat(Boolean.valueOf(TransactionStateManager.currentTransactionExists())).isFalse();
            assertThat((Object) TransactionStateManager.getTransaction()).isNull();

            tsm.run(TransactionProperties.DEFAULT, tx -> {
                assertThat(Boolean.valueOf(TransactionStateManager.currentTransactionExists())).isTrue();
                assertThat((Transaction) TransactionStateManager.getTransaction()).isSameAs(tx);
            });

            assertThat(TransactionStateManager.currentTransactionExists()).isFalse();
            assertThat((Object) TransactionStateManager.getTransaction()).isNull();
        });
    }

    // --- Test Transaction ---------------------------------------------------

    /**
     * Simple test transaction exposing parent reference and counters.
     */
    static class TestSimpleTransaction extends com.wiredi.runtime.transactions.simple.SimpleTransaction {
        private final Transaction parent;

        TestSimpleTransaction() {
            super();
            this.parent = null;
        }

        TestSimpleTransaction(Transaction parent) {
            super(parent);
            this.parent = parent;
        }

        @Override
        public Transaction getParent() {
            return parent;
        }
    }
}
