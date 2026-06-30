package com.wiredi.compiler.domain.annotations;

import com.wiredi.compiler.domain.Annotations;
import com.wiredi.logging.Logging;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

public class AnnotationProxy<T extends Annotation> implements InvocationHandler {

    @NotNull
    private final Elements elements;
    @NotNull
    private final Types types;
    @NotNull
    private final Class<T> annotationType;
    @NotNull
    private final Map<String, Object> valuesByName;

    private static final Logging logger = Logging.getInstance(AnnotationProxy.class);

    public AnnotationProxy(
            @NotNull Elements elements,
            @NotNull Types types,
            @NotNull Class<T> annotationType,
            @NotNull Map<String, Object> valuesByName
    ) {
        this.elements = elements;
        this.types = types;
        this.annotationType = annotationType;
        this.valuesByName = valuesByName;
    }

    @Nullable
    public String getQualifiedNameFromTypeMirror(TypeMirror typeMirror) {
        if (typeMirror.getKind() != TypeKind.DECLARED) return null;
        DeclaredType declaredType = (DeclaredType) typeMirror;
        Element element = declaredType.asElement();
        if (!(element instanceof TypeElement)) return null;
        return ((TypeElement) element).getQualifiedName().toString();
    }

    @Nullable
    public TypeMirror toTypeMirror(@NotNull Class<?> clazz) {
        // Array-Typen rekursiv über den Komponententyp auflösen
        if (clazz.isArray()) {
            Class<?> componentClass = clazz.getComponentType();
            TypeMirror componentMirror = toTypeMirror(componentClass);
            if (componentMirror == null) {
                return null;
            }
            return types.getArrayType(componentMirror);
        }

        // Normale deklariere Typen
        TypeElement typeElement = elements.getTypeElement(clazz.getCanonicalName());
        if (typeElement == null) {
            return null;
        }
        return typeElement.asType();
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();

        // Object-Methoden korrekt behandeln
        if (methodName.equals("toString") && method.getParameterCount() == 0) {
            return annotationType.getName() + valuesByName;
        }
        if (methodName.equals("hashCode") && method.getParameterCount() == 0) {
            return annotationHashCode(annotationType, valuesByName);
        }
        if (methodName.equals("equals") && method.getParameterCount() == 1) {
            return annotationEquals(annotationType, valuesByName, args[0]);
        }
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        }

        Class<?> returnType = method.getReturnType();

        // Wert bestimmen: expliziter Wert aus dem Mirror oder Default-Wert aus der Annotation-Definition
        Object storedValue = valuesByName.containsKey(methodName)
                ? valuesByName.get(methodName)
                : method.getDefaultValue();

        Object finalStoredValue = storedValue;
        logger.info(() -> "Invoking '" + returnType + " " + methodName + "()'" +
                " on " + annotationType + ". Raw value = " + finalStoredValue + ".");

        // 1) Class-Attribut: immer MirroredTypeException werfen
        if (returnType == Class.class) {
            javax.lang.model.type.TypeMirror tm = null;

            if (storedValue instanceof javax.lang.model.type.TypeMirror m) {
                tm = m;
            } else if (storedValue instanceof Class<?> clazz) {
                tm = toTypeMirror(clazz);
            }

            if (tm == null) {
                throw new IllegalStateException(
                        "Cannot resolve TypeMirror for Class-valued attribute " +
                                annotationType.getName() + "." + methodName + " with value " + storedValue);
            }

            throw new javax.lang.model.type.MirroredTypeException(tm);
        }


        // 2) Class[]-Attribut: immer MirroredTypesException werfen
        if (returnType.isArray() && returnType.getComponentType() == Class.class) {
            java.util.List<javax.lang.model.type.TypeMirror> mirrors = new java.util.ArrayList<>();

            if (storedValue instanceof java.util.List<?> list) {
                // typischer Fall: List<AnnotationValue> aus javac
                for (Object o : list) {
                    Object v = o;
                    if (o instanceof javax.lang.model.element.AnnotationValue av) {
                        v = av.getValue();
                    }
                    if (v instanceof javax.lang.model.type.TypeMirror tm) {
                        mirrors.add(tm);
                    } else if (v instanceof Class<?> clazz) {
                        javax.lang.model.type.TypeMirror tm = toTypeMirror(clazz);
                        if (tm != null) {
                            mirrors.add(tm);
                        }
                    }
                }
            } else if (storedValue instanceof Class<?>[] classes) {
                for (Class<?> clazz : classes) {
                    javax.lang.model.type.TypeMirror tm = toTypeMirror(clazz);
                    if (tm != null) {
                        mirrors.add(tm);
                    }
                }
            }

            if (mirrors.isEmpty()) {
                throw new IllegalStateException(
                        "Cannot resolve TypeMirrors for Class[]-valued attribute " +
                                annotationType.getName() + "." + methodName + " with value " + storedValue);
            }

            throw new javax.lang.model.type.MirroredTypesException(mirrors);
        }            // 3) Verschachtelte Annotation (kein Array): AnnotationMirror -> Proxy-Annotation
        if (returnType.isAnnotation()) {
            if (storedValue instanceof AnnotationMirror nestedMirror) {
                @SuppressWarnings("unchecked")
                Class<? extends Annotation> nestedType = (Class<? extends Annotation>) returnType;
                return Annotations.proxy(nestedMirror, nestedType);
            }
            if (storedValue != null && returnType.isInstance(storedValue)) {
                // z.B. Defaults in Tests, wo echte Annotation-Instanzen hinterlegt sind
                return storedValue;
            }

            throw new IllegalStateException(
                    "No value or incompatible default for nested annotation attribute " +
                            annotationType.getName() + "." + methodName + ": " + storedValue);
        }

        // 4) Andere Array-Attribute (z.B. String[], int[], Enum[], Annotation[])
        if (returnType.isArray()) {
            Class<?> componentType = returnType.getComponentType();

            // javac: List<AnnotationValue>
            if (storedValue instanceof java.util.List<?> list) {
                int size = list.size();
                Object array = java.lang.reflect.Array.newInstance(componentType, size);

                for (int i = 0; i < size; i++) {
                    Object element = list.get(i);
                    Object value = (element instanceof javax.lang.model.element.AnnotationValue av)
                            ? av.getValue()
                            : element;

                    // Verschachtelte Annotationen im Array: AnnotationMirror -> Proxy-Annotation
                    if (componentType.isAnnotation() && value instanceof AnnotationMirror nestedMirror) {
                        @SuppressWarnings("unchecked")
                        Class<? extends Annotation> nestedType = (Class<? extends Annotation>) componentType;
                        value = Annotations.proxy(nestedMirror, nestedType);
                    }

                    int finalI = i;
                    Object finalValue = value;
                    logger.info(() -> "Array element " + finalI + " of " + annotationType + "." + methodName + " is " + finalValue);
                    java.lang.reflect.Array.set(array, i, value);
                }
                return array;
            }
        }


            // 4) Skalare Nicht-Class-Attribute (String, primitive, Enum, ...)
        if (storedValue == null) {
            // Das dürfte zur Laufzeit nicht vorkommen (entweder Wert oder Default muss existieren),
            // aber wir failen lieber hart als ein null für primitive zurückzugeben.
            throw new IllegalStateException(
                    "No value and no default for scalar attribute " +
                            annotationType.getName() + "." + methodName);
        }

        return storedValue;
    }

    private int annotationHashCode(Class<?> annotationType, Map<String, Object> valuesByName) {
        // Sehr vereinfachte Version: Name + Werte-Map
        // (Für 100% korrekte Semantik müsste man die Regeln aus java.lang.annotation.Annotation implementieren)
        int result = annotationType.getName().hashCode();
        result = 31 * result + valuesByName.hashCode();
        return result;
    }

    private boolean annotationEquals(Class<?> annotationType,
                                     Map<String, Object> valuesByName,
                                     Object other) {
        if (other == this) {
            return true;
        }
        if (!annotationType.isInstance(other)) {
            return false;
        }
        // Vereinfach: Werte per Reflektion vergleichen
        for (Method m : annotationType.getDeclaredMethods()) {
            if (m.getParameterCount() > 0) {
                continue;
            }
            String name = m.getName();
            Object thisValue = valuesByName.containsKey(name)
                    ? valuesByName.get(name)
                    : m.getDefaultValue();
            Object otherValue;
            try {
                otherValue = m.invoke(other);
            } catch (Exception e) {
                return false;
            }
            if (!java.util.Objects.deepEquals(thisValue, otherValue)) {
                return false;
            }
        }
        return true;
    }
}
