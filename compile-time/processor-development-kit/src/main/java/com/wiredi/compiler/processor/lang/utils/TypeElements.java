package com.wiredi.compiler.processor.lang.utils;

import com.wiredi.runtime.collections.TypeMap;
import com.wiredi.compiler.errors.ProcessingException;
import com.wiredi.compiler.logger.Logger;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.*;

public class TypeElements {

    private static final Map<TypeElement, List<? extends Element>> MEMBER_CACHE = new HashMap<>();
    private static final Map<TypeElement, List<? extends VariableElement>> FIELD_CACHE = new HashMap<>();
    private static final Map<TypeElement, List<ExecutableElement>> METHOD_CACHE = new HashMap<>();
    private static final Map<ExecutableElement, List<VariableElement>> PARAMETER_CACHE = new HashMap<>();
    private static final Logger logger = Logger.get(TypeElements.class);
    private static final TypeMap<TypeMirror> TYPE_CACHE = new TypeMap<>();
    private final Elements elements;
    private final Types types;

    public TypeElements(Elements elements, Types types) {
        this.elements = elements;
        this.types = types;
    }

    public boolean iSOfType(TypeMirror typeMirror, Class<?> type) {
        return types.erasure(TYPE_CACHE.computeIfAbsent(type, () -> elements.getTypeElement(type.getName()).asType())).toString()
                .equals(types.erasure(typeMirror).toString());
    }

    public TypeElement outerMostTypeElementOf(Element element) {
        if (element.getKind() == ElementKind.PACKAGE || element.getKind() == ElementKind.MODULE) {
            throw new IllegalArgumentException("Cannot determine an outer most class of a package or module!");
        }

        Element inner = element;
        Element outer = inner.getEnclosingElement();
        while (outer.getKind() != ElementKind.PACKAGE) {
            inner = outer;
            outer = inner.getEnclosingElement();
        }

        if (outer instanceof TypeElement typeElement) {
            return typeElement;
        } else {
            throw new IllegalStateException("The outer most element was not a TypeElement. Instead it was a " + outer);
        }
    }

    public List<? extends VariableElement> fieldsOf(TypeElement typeElement) {
        synchronized (FIELD_CACHE) {
            return FIELD_CACHE.computeIfAbsent(typeElement, element -> membersOf(element).stream()
                    .filter(it -> it.getKind() == ElementKind.FIELD)
                    .map(it -> (VariableElement) it)
                    .toList()
            );
        }
    }

    public List<? extends ExecutableElement> methodsOf(TypeElement typeElement) {
        synchronized (METHOD_CACHE) {
            if (METHOD_CACHE.get(typeElement) == null) {
                List<? extends ExecutableElement> methodsOf = determineMethodsOf(typeElement);
                METHOD_CACHE.computeIfAbsent(typeElement, (t) -> new ArrayList<>()).addAll(methodsOf);
            }
            return METHOD_CACHE.get(typeElement);
        }
    }

    public List<? extends VariableElement> parametersOf(ExecutableElement method) {
        synchronized (PARAMETER_CACHE) {
            return PARAMETER_CACHE.computeIfAbsent(method, it -> it.getEnclosedElements()
                    .stream()
                    .filter(parameter -> parameter.getKind() == ElementKind.PARAMETER)
                    .map(parameter -> (VariableElement) parameter)
                    .toList());
        }
    }

    private List<? extends ExecutableElement> determineMethodsOf(TypeElement typeElement) {
        Set<ExecutableElement> members = new HashSet<>(
                typeElement.getEnclosedElements()
                        .stream()
                        .filter(it -> it.getKind() == ElementKind.METHOD)
                        .map(it -> (ExecutableElement) it)
                        .toList()
        );
        if (typeElement.getSuperclass().getKind() != TypeKind.NONE) {
            TypeElement superClass = (TypeElement) types.asElement(typeElement.getSuperclass());
            for (ExecutableElement inherited : methodsOf(superClass)) {
                if (members.stream().noneMatch(member -> overrides(inherited, member))) {
                    members.add(inherited);
                }
            }
        }

        return new ArrayList<>(members);
    }

    public boolean overrides(ExecutableElement overrider, ExecutableElement overridden) {
        // Override will only compile if the method overrides something
        if (overrider.getAnnotation(Override.class) != null) {
            return true;
        }

        // Private functions are never functionally overwritten
        if (overridden.getModifiers().contains(Modifier.PRIVATE)
                || overrider.getModifiers().contains(Modifier.PRIVATE)) {
            return false;
        }

        // Methods that are package private, are only overwritten
        // if the two defining classes are in the same package
        if (!containsAny(overrider.getModifiers(), Modifier.PUBLIC, Modifier.PROTECTED)) {
            if (!elements.getPackageOf(overrider).equals(elements.getPackageOf(overridden))) {
                return false;
            }
        }

        // After checking the location and relation between the overrider and
        // the overridden, the last criteria is based on name, parameters
        // and return value.
        return overrider.getSimpleName().equals(overridden.getSimpleName())
                && overrider.getReturnType().equals(overridden.getReturnType())
                && overrider.getTypeParameters().equals(overridden.getTypeParameters());
    }

    public <S> boolean containsAny(Collection<S> c, S... elements) {
        List<S> matches = Arrays.asList(elements);
        for (S s : c) {
            if (matches.contains(s)) {
                return true;
            }
        }

        return false;
    }

    @NotNull
    public List<? extends Element> membersOf(TypeElement typeElement) {
        synchronized (MEMBER_CACHE) {
            return MEMBER_CACHE.computeIfAbsent(typeElement, this::findAllMembersOf);
        }
    }

    private List<? extends Element> findAllMembersOf(TypeElement typeElement) {
        if (typeElement.toString().equals(Object.class.getName())) {
            return Collections.emptyList();
        }

        List<Element> result = findAllInheritedMembersOf(typeElement);
        result.addAll(typeElement.getEnclosedElements());
        return result;
    }

    private List<Element> findAllInheritedMembersOf(TypeElement typeElement) {
        TypeMirror superclass = typeElement.getSuperclass();
        List<Element> result = new ArrayList<>();
        if (superclass.toString().equals(Object.class.getName())) {
            return result;
        }
        if (superclass.getKind() == TypeKind.NONE) {
            return result;
        }

        TypeElement superClassType = (TypeElement) types.asElement(superclass);
        result.addAll(elements.getAllMembers(superClassType));
        result.addAll(findAllInheritedMembersOf(superClassType));
        return result;
    }

    @NotNull
    public <T> Class<T> asClass(TypeElement typeElement) {
        try {
            return (Class<T>) Class.forName(getClassName(typeElement));
        } catch (Exception e) {
            throw new ProcessingException(typeElement, "Could not instantiate class. Make sure that this class has no or one constructor without parameters!");
        }
    }

    @NotNull
    public String getClassName(TypeElement element) {
        Element currElement = element;
        StringBuilder result = new StringBuilder(element.getSimpleName().toString());
        while (currElement.getEnclosingElement() != null) {
            currElement = currElement.getEnclosingElement();
            if (currElement instanceof TypeElement) {
                result.insert(0, currElement.getSimpleName() + "$");
            } else if (currElement instanceof PackageElement) {
                if (!currElement.getSimpleName().contentEquals("")) {
                    result.insert(0, ((PackageElement) currElement)
                            .getQualifiedName() + ".");
                }
            }
        }
        return result.toString();
    }
}
