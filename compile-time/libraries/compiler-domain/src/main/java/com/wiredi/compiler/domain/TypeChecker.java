package com.wiredi.compiler.domain;

import com.wiredi.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.beans.Bean;
import jakarta.inject.Provider;

import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Collection;
import java.util.List;

public class TypeChecker {

	private final TypeMirror providerTypeElement;
	private final TypeMirror nativeProviderTypeElement;
	private final TypeMirror collectionTypeElement;
	private final TypeMirror listTypeElement;
	private final TypeMirror beanTypeElement;
	private final Types types;

	public TypeChecker(Elements elements, Types types) {
		this.providerTypeElement = elements.getTypeElement(Provider.class.getName()).asType();
		this.nativeProviderTypeElement = elements.getTypeElement(IdentifiableProvider.class.getName()).asType();
		this.collectionTypeElement = elements.getTypeElement(Collection.class.getName()).asType();
		this.listTypeElement = elements.getTypeElement(List.class.getName()).asType();
		this.beanTypeElement = elements.getTypeElement(Bean.class.getName()).asType();
		this.types = types;
	}

	public Checker theType(TypeMirror typeMirror) {
		return new Checker(typeMirror);
	}

	public class Checker {

		private final TypeMirror typeMirror;

		public Checker(TypeMirror typeMirror) {
			this.typeMirror = typeMirror;
		}

		public boolean isProvider() {
			return types.isAssignable(providerTypeElement, types.erasure(typeMirror));
		}

		public boolean isNativeProvider() {
			return types.isAssignable(nativeProviderTypeElement, types.erasure(typeMirror));
		}

		public boolean isCollection() {
			return types.isAssignable(collectionTypeElement, types.erasure(typeMirror));
		}

		public boolean isList() {
			return types.isAssignable(listTypeElement, types.erasure(typeMirror));
		}

		public boolean isBean() {
			return types.isAssignable(beanTypeElement, types.erasure(typeMirror));
		}
	}
}
