package com.wiredi.aop;

import com.wiredi.annotations.PrimaryWireType;
import com.wiredi.annotations.Wire;
import com.wiredi.runtime.WireContainer;
import com.wiredi.runtime.aspects.AspectHandler;
import com.wiredi.runtime.aspects.ExecutionChain;
import com.wiredi.runtime.aspects.ExecutionChainRegistry;
import com.wiredi.runtime.aspects.RootMethod;
import com.wiredi.runtime.domain.annotations.AnnotationMetadata;
import com.wiredi.runtime.domain.aop.AspectAwareProxy;
import com.wiredi.runtime.values.Value;
import jakarta.annotation.Generated;
import java.lang.Object;
import java.lang.Override;

@PrimaryWireType(ProxyTarget.class)
@Generated(
        value = "com.wiredi.compiler.domain.entities.AspectAwareProxyEntity",
        date = "2023-01-01T00:00Z"
)
@Wire(
        to = {Interface.class, Object.class, ProxyTarget$$AspectAwareProxy.class},
        singleton = true,
        proxy = false
)
final class ProxyTarget$$AspectAwareProxy extends ProxyTarget implements AspectAwareProxy {
    private final WireContainer wireContainer;

    private final Value<ExecutionChain> executionChain;

    ProxyTarget$$AspectAwareProxy(final ExecutionChainRegistry executionChainRegistry,
                          final WireContainer wireContainer) {
        this.wireContainer = wireContainer;
        this.executionChain = Value.async(() ->
                executionChainRegistry.getExecutionChain(
                        RootMethod.builder("toProxy")
                                .withAnnotation(AnnotationMetadata.empty("com.wiredi.aop.Transactional"))
                                .build(AspectHandler.wrap((c) -> super.toProxy()))
                )

        );
    }

    @Transactional
    @Override
    public final void toProxy() {
        executionChain.get()
                .execute()
                .andReturn();
    }
}
