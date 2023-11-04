package com.wiredi.compiler.domain;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.wiredi.domain.provider.IdentifiableProvider;
import com.wiredi.lang.collections.TypeMap;
import com.wiredi.runtime.beans.Bean;
import jakarta.inject.Provider;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Collection;
import java.util.List;

public class TypeChecker {

	private final TypeMap<TypeMirror> typesCache = new TypeMap<>();
	private final TypeMirror providerTypeElement;
	private final TypeMirror nativeProviderTypeElement;
	private final TypeMirror collectionTypeElement;
	private final TypeMirror listTypeElement;
	private final TypeMirror beanTypeElement;
	private final Types types;
	private final Elements elements;

	public TypeChecker(Elements elements, Types types) {
		this.providerTypeElement = elements.getTypeElement(Provider.class.getName()).asType();
		this.nativeProviderTypeElement = elements.getTypeElement(IdentifiableProvider.class.getName()).asType();
		this.collectionTypeElement = elements.getTypeElement(Collection.class.getName()).asType();
		this.listTypeElement = elements.getTypeElement(List.class.getName()).asType();
		this.beanTypeElement = elements.getTypeElement(Bean.class.getName()).asType();
		this.types = types;
		this.elements = elements;
	}

	public Checker theType(Element element) {
		return new Checker(element.asType());
	}

	public Checker theType(TypeMirror typeMirror) {
		return new Checker(typeMirror);
	}

	private TypeMirror asTypeMirror(Class<?> type) {
		synchronized (typesCache) {
			return typesCache.computeIfAbsent(type, () -> elements.getTypeElement(type.getName()).asType());
		}
	}

	private boolean isAssignable(TypeMirror base, TypeMirror request) {
		return types.isAssignable(base, types.erasure(request));
	}

	private boolean isAssignable(Class<?> base, TypeMirror request) {
		return isAssignable(asTypeMirror(base), types.erasure(request));
	}

	public class Checker {

		private final TypeMirror typeMirror;

		public Checker(TypeMirror typeMirror) {
			this.typeMirror = typeMirror;
		}

		public boolean isProvider() {
			return isAssignable(Provider.class, typeMirror);
		}

		public boolean isNativeProvider() {
			return isAssignable(IdentifiableProvider.class, typeMirror);
		}

		public boolean isCollection() {
			return isAssignable(Collection.class, typeMirror);
		}

		public boolean isList() {
			return isAssignable(List.class, typeMirror);
		}

		public boolean isBean() {
			return isAssignable(Bean.class, typeMirror);
		}

		public boolean isOf(Class<?> type) {
			return isAssignable(type, typeMirror);
		}

		public TypeName typeName() {
			return ClassName.get(typeMirror);
		}
	}
}
