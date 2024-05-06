package com.wiredi.aop;

import com.wiredi.annotations.PrimaryWireType;
import com.wiredi.annotations.Wire;
import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.aspects.AspectHandler;
import com.wiredi.runtime.aspects.ExecutionChain;
import com.wiredi.runtime.aspects.links.RootMethod;
import com.wiredi.runtime.domain.AnnotationMetaData;
import com.wiredi.runtime.domain.aop.AspectAwareProxy;
import com.wiredi.runtime.values.Value;
import jakarta.annotation.Generated;
import java.lang.Object;
import java.lang.Override;
import java.util.List;

@PrimaryWireType(ProxyTarget.class)
@Generated(
        value = "com.wiredi.compiler.domain.entities.AspectAwareProxyEntity",
        date = "2023-01-01T00:00Z"
)
@Wire(
        to = {Interface.class, Object.class},
        singleton = true,
        proxy = false
)
final class ProxyTarget$$AspectAwareProxy extends ProxyTarget implements AspectAwareProxy {
    private final WireRepository wireRepository;

    private final Value<ExecutionChain> executionChain0;

    ProxyTarget$$AspectAwareProxy(final List<AspectHandler> aspectHandlers,
                                  final WireRepository wireRepository) {
        this.wireRepository = wireRepository;
        this.executionChain0 = Value.async(() ->
                ExecutionChain.newInstance(
                                RootMethod.newInstance("toProxy")
                                        .withAnnotation(AnnotationMetaData.empty("com.wiredi.aop.Transactional"))
                                        .build(AspectHandler.wrap((c) -> super.toProxy()))
                        )
                        .withProcessors(aspectHandlers)
                        .build()
        );
    }

    @Override
    @Transactional
    public final void toProxy() {
        executionChain0.get()
                .execute()
                .andReturn();
    }
}
