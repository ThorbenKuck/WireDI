package com.wiredi.processor.tck.domain.transactional;

import com.wiredi.annotations.Wire;
import com.wiredi.aspects.AspectHandler;
import com.wiredi.aspects.ExecutionContext;
import com.wiredi.aspects.links.RootMethod;
import com.wiredi.domain.AnnotationMetaData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Wire
public class CustomTransactionalAspectHandler implements AspectHandler {
	@Override
	public @Nullable Object process(@NotNull ExecutionContext context) {
		return "[TRANSACTIONAL]: " + context.proceed();
	}

	@Override
	public boolean appliesTo(AnnotationMetaData annotationMetaData, RootMethod rootMethod) {
		return Objects.equals(annotationMetaData.className(), Transactional.class.getName());
	}
}
