package com.wiredi.lang.eager;

import com.wiredi.lang.async.Barrier;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class EagerList<T> {

	private final List<T> content = new ArrayList<>();
	private final Barrier barrier = new Barrier();

	public EagerList(Supplier<List<T>> supplier) {
		EagerLoadingContext.load(supplier, result -> {
			this.content.addAll(result);
			barrier.open();
			try {
				result.clear(); // Prevent Memory Leaks
			} catch (UnsupportedOperationException ignored) {
				// If this exception happens, the service loader produced an
				// unexpected ImmutableCollection. This is unrecoverable
				// but also not critical. It just means that we do not have
				// the power to try and clean up the leftover resources.
			}
		});
	}

	public void await() {
		barrier.traverse();
	}

	public void forEach(Consumer<T> consumer) {
		barrier.traverse();
		content.forEach(consumer);
	}

	public List<T> content() {
		barrier.traverse();
		return content;
	}
}
