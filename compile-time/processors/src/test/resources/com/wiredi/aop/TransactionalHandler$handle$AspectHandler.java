package com.wiredi.aop;

import com.wiredi.annotations.Wire;
import com.wiredi.runtime.aspects.AspectHandler;
import com.wiredi.runtime.aspects.ExecutionContext;
import com.wiredi.runtime.aspects.RootMethod;
import com.wiredi.runtime.domain.AnnotationMetaData;
import jakarta.annotation.Generated;
import java.lang.Object;
import java.lang.Override;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Wire
@Generated(
        value = "com.wiredi.compiler.domain.entities.AspectHandlerEntity",
        date = "2023-01-01T00:00Z"
)
public final class TransactionalHandler$handle$AspectHandler implements AspectHandler {
    private final TransactionalHandler delegate;

    protected TransactionalHandler$handle$AspectHandler(TransactionalHandler delegate) {
        this.delegate = delegate;
    }

    @Override
    @Nullable
    public final Object process(@NotNull final ExecutionContext context) {
        return delegate.handle(context);
    }

    public final boolean appliesTo(@NotNull final AnnotationMetaData annotation,
            @NotNull final RootMethod rootMethod) {
        return annotation.className().equals("com.wiredi.aop.Transactional");
    }
}
