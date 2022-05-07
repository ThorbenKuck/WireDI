package com.github.thorbenkuck.di.processor.builder.constructors.factory;

import com.github.thorbenkuck.di.processor.AnnotationTypeFieldExtractor;
import com.github.thorbenkuck.di.processor.Logger;
import com.github.thorbenkuck.di.processor.ProcessorContext;
import com.github.thorbenkuck.di.processor.utils.WiredTypeUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class InjectionContext {

    private final List<Field> fieldInjections = new ArrayList<>();
    private final List<Setter> setterInjections = new ArrayList<>();
    private final List<ConstructorParameter> constructorParameters = new ArrayList<>();
    private final List<PostConstructMethod> postConstructMethods = new ArrayList<>();
    private boolean returnAdded = false;

    public List<Field> drainFieldInjections() {
        ArrayList<Field> result = new ArrayList<>(fieldInjections);
        fieldInjections.clear();

        return result;
    }

    public List<Setter> drainSetterInjections() {
        ArrayList<Setter> result = new ArrayList<>(setterInjections);
        setterInjections.clear();

        return result;
    }

    public List<ConstructorParameter> drainConstructorParameters() {
        ArrayList<ConstructorParameter> result = new ArrayList<>(constructorParameters);
        constructorParameters.clear();

        return result;
    }

    public List<PostConstructMethod> drainPostConstructMethod() {
        ArrayList<PostConstructMethod> result = new ArrayList<>(postConstructMethods);
        postConstructMethods.clear();

        return result;
    }

    public void markAsReturned() {
        returnAdded = true;
    }

    public boolean alreadyReturned() {
        return returnAdded;
    }

    public boolean hasNoFieldInjections() {
        return fieldInjections.isEmpty();
    }

    public boolean hasNoSetterInjections() {
        return setterInjections.isEmpty();
    }

    public boolean hasNoConstructorInjections() {
        return constructorParameters.isEmpty();
    }

    public boolean hasNoPostConstructMethod() {
        return postConstructMethods.isEmpty();
    }

    public boolean hasNoFurtherInjectionQualities() {
        return hasNoFieldInjections()
                && hasNoSetterInjections()
                && hasNoConstructorInjections()
                && hasNoPostConstructMethod();
    }

    public boolean hasFurtherInjectionQualities() {
        return !hasNoFurtherInjectionQualities();
    }

    public void announceFieldInjection(VariableElement variableElement) {
        if(WiredTypeUtils.isWireCandidate(variableElement.asType())) {
            Logger.warn(variableElement, "The declared type %s is not declared as a wire candidate", variableElement.asType().toString());
        }

        fieldInjections.add(new Field(variableElement));
    }

    public void announceSetterInjection(ExecutableElement method) {
        method.getParameters().forEach(variableElement -> {
            if(WiredTypeUtils.isWireCandidate(variableElement.asType())) {
                Logger.warn(variableElement, "The declared type %s is not declared as a wire candidate", variableElement.asType().toString());
            }
        });

        setterInjections.add(new Setter(method));
    }

    public void announceConstructorParameter(VariableElement variableElement) {
        if(WiredTypeUtils.isWireCandidate(variableElement.asType())) {
            Logger.warn(variableElement, "The declared type %s is not declared as a wire candidate", variableElement.asType().toString());
        }

        constructorParameters.add(new ConstructorParameter(variableElement, constructorParameters.size()));
    }

    public void announcePostConstructMethod(ExecutableElement executableElement) {
        postConstructMethods.add(new PostConstructMethod(executableElement));
    }

    private static String convertedDefault(String input) {
        if (input == null) {
            return null;
        }

        if (input.isEmpty()) {
            return null;
        }

        return input;
    }

    public List<PostConstructMethod> getPostConstructMethods() {
        return postConstructMethods;
    }

    public class Field {
        private final Name name;
        private final VariableElement variableElement;
        private final TypeElement type;
        private final boolean mayBeNull;
        private final boolean requiresReflection;

        public Field(VariableElement variableElement) {
            this.variableElement = variableElement;
            this.name = variableElement.getSimpleName();
            this.type = (TypeElement) ProcessorContext.getTypes().asElement(variableElement.asType());
            this.mayBeNull = AnnotationTypeFieldExtractor.hasAnnotationByName(variableElement, "Nullable");
            this.requiresReflection = variableElement.getModifiers().contains(Modifier.PRIVATE);
        }

        public Name getName() {
            return name;
        }

        public VariableElement getVariableElement() {
            return variableElement;
        }

        public TypeElement getType() {
            return type;
        }

        public boolean mayBeNull() {
            return mayBeNull;
        }

        public boolean requiresReflection() {
            return requiresReflection;
        }
    }

    public class Setter {

        private final ExecutableElement executableElement;
        private final TypeElement type;
        private final Name methodName;
        private final boolean mayBeNull;
        private final boolean requiresReflection;

        public Setter(ExecutableElement executableElement) {
            if (executableElement.getParameters().size() != 1) {
                throw new IllegalArgumentException("Setters may only have one parameter");
            }
            VariableElement parameter = executableElement.getParameters().get(0);
            this.executableElement = executableElement;
            this.methodName = executableElement.getSimpleName();
            this.type = (TypeElement) ProcessorContext.getTypes().asElement(parameter.asType());
            this.mayBeNull = AnnotationTypeFieldExtractor.hasAnnotationByName(executableElement, "nullable")
                    || AnnotationTypeFieldExtractor.hasAnnotationByName(parameter, "nullable");
            this.requiresReflection = executableElement.getModifiers().contains(Modifier.PRIVATE);
        }

        public ExecutableElement getExecutableElement() {
            return executableElement;
        }

        public TypeElement getType() {
            return type;
        }

        public Name getMethodName() {
            return methodName;
        }

        public boolean mayBeNull() {
            return mayBeNull;
        }

        public boolean requiresReflection() {
            return requiresReflection;
        }
    }

    public class ConstructorParameter {
        private final VariableElement variableElement;
        private final TypeElement type;
        private final Name name;
        private final boolean mayBeNull;
        private final int index;

        public ConstructorParameter(VariableElement variableElement, int index) {
            this.variableElement = variableElement;
            this.name = variableElement.getSimpleName();
            this.type = (TypeElement) ProcessorContext.getTypes().asElement(variableElement.asType());
            this.mayBeNull = AnnotationTypeFieldExtractor.hasAnnotationByName(variableElement, "Nullable");
            this.index = index;
        }

        public VariableElement getVariableElement() {
            return variableElement;
        }

        public TypeElement getType() {
            return type;
        }

        public Name getName() {
            return name;
        }

        public boolean mayBeNull() {
            return mayBeNull;
        }

        public int getIndex() {
            return index;
        }
    }

    public class PostConstructMethod {
        private final ExecutableElement executableElement;
        private final List<? extends VariableElement> parameters;
        private final Name methodName;
        private final boolean requiresReflection;

        public PostConstructMethod(ExecutableElement executableElement) {
            this.executableElement = executableElement;
            this.methodName = executableElement.getSimpleName();
            this.parameters = executableElement.getParameters();
            this.requiresReflection = executableElement.getModifiers().contains(Modifier.PRIVATE);
        }

        public ExecutableElement getExecutableElement() {
            return executableElement;
        }

        public Name getMethodName() {
            return methodName;
        }

        public boolean requiresReflection() {
            return requiresReflection;
        }

        public List<Parameter> determineParameters() {
            return parameters.stream().map(Parameter::new).collect(Collectors.toList());
        }

        public TypeName returnType() {
            return ClassName.get(executableElement.getReturnType());
        }

        class Parameter {
            private final VariableElement variableElement;
            private final Name variableName;
            private final boolean mayBeNull;

            Parameter(VariableElement variableElement) {
                this.variableElement = variableElement;
                this.variableName = variableElement.getSimpleName();
                this.mayBeNull = AnnotationTypeFieldExtractor.hasAnnotationByName(variableElement, "Nullable");
            }

            public Name getVariableName() {
                return variableName;
            }

            public boolean mayBeNull() {
                return mayBeNull;
            }

            public VariableElement getVariableElement() {
                return variableElement;
            }

            public TypeElement getVariableType() {
                return (TypeElement) ProcessorContext.getTypes().asElement(variableElement.asType());
            }
        }
    }
}