package com.github.thorbenkuck.di.processor.builder;

import com.github.thorbenkuck.di.domain.IdentifiableProvider;
import com.github.thorbenkuck.di.domain.WireRepository;
import com.github.thorbenkuck.di.processor.WireInformation;
import com.github.thorbenkuck.di.processor.constructors.MethodConstructor;
import com.github.thorbenkuck.di.processor.constructors.factory.CreateInstanceForPropertySourceMethodConstructor;
import com.github.thorbenkuck.di.processor.constructors.factory.CreateInstanceForWireMethodConstructor;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.concurrent.atomic.AtomicBoolean;

public class IdentifiableProviderClassBuilder extends ClassBuilder {

    private final WireInformation wireInformation;

    private static final ClassName PROVIDER_CLASS_NAME = ClassName.get(IdentifiableProvider.class);
    private static final AnnotationSpec AUTO_SERVICE_ANNOTATION_SPEC = AnnotationSpec.builder(AutoService.class)
            .addMember("value", "$T.class", IdentifiableProvider.class)
            .build();

    public IdentifiableProviderClassBuilder(WireInformation wireInformation) {
        super(wireInformation);
        this.wireInformation = wireInformation;
        setup();
    }

    @Override
    protected TypeSpec.Builder initialize() {
        return TypeSpec.classBuilder(wireInformation.simpleClassName() + "IdentifiableProvider")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(ParameterizedTypeName.get(PROVIDER_CLASS_NAME, wireInformation.primaryClassName()))
                .addAnnotation(AUTO_SERVICE_ANNOTATION_SPEC);
    }

    public IdentifiableProviderClassBuilder overwriteAllRequiredMethods() {
        return addPriorityMethod()
                .addSingletonMethod()
                .addTypeMethod()
                .addWiredTypesMethod()
                .addGetMethod();
    }

    public IdentifiableProviderClassBuilder addPriorityMethod() {
        wireInformation.getWirePriority().ifPresent(priority -> {
            addMethod(
                    overwriteMethod("priority")
                            .returns(int.class)
                            .addCode(CodeBlock.builder().addStatement("return $L", priority).build())
            );

        });

        return this;
    }

    public IdentifiableProviderClassBuilder addSingletonMethod() {
        boolean singleton = wireInformation.isSingleton();
        addMethod(
                overwriteMethod("isSingleton")
                        .returns(TypeName.BOOLEAN)
                        .addCode(CodeBlock.builder().addStatement("return $L", singleton).build())
        );

        return this;
    }

    public IdentifiableProviderClassBuilder addTypeMethod() {
        TypeElement primaryWireType = wireInformation.getPrimaryWireType();
        String fieldName = "PRIMARY_WIRE_TYPE";
        addMethod(
                overwriteMethod("type")
                        .returns(ClassName.get(Class.class))
                        .addStatement("return $L", fieldName)
        );

        addField(
                classConstant(genericClass(primaryWireType), fieldName)
                        .initializer("$T.class", ClassName.get(primaryWireType))
        );

        return this;
    }

    public IdentifiableProviderClassBuilder addWiredTypesMethod() {
        CodeBlock.Builder initializer = CodeBlock.builder()
                .add("new Class[] { ");
        final AtomicBoolean first = new AtomicBoolean(true);
        final String fieldName = "ALL_WIRED_TYPES";

        wireInformation.getAllWireCandidates()
                .forEach(it -> {
                    if (first.get()) {
                        initializer.add("$T.class", ClassName.get(it));
                        first.set(false);
                    } else {
                        initializer.add(", $T.class", ClassName.get(it));
                    }
                });

        addField(
                classConstant(TypeName.get(Class[].class), fieldName)
                        .initializer(initializer.add(" }").build())
        );

        addMethod(
                overwriteMethod("wiredTypes")
                        .addStatement("return $L", fieldName)
                        .returns(TypeName.get(Class[].class))
        );

        return this;
    }

    public IdentifiableProviderClassBuilder addGetMethod() {
        final String fieldName = "instance";
        MethodSpec.Builder getMethodBuilder = overwriteMethod("get")
                .addParameter(ClassName.get(WireRepository.class), "wiredTypes")
                .returns(TypeName.get(wireInformation.getPrimaryWireType().asType()));

        if (wireInformation.isSingleton()) {
            addField(
                    FieldSpec.builder(TypeName.get(wireInformation.getPrimaryWireType().asType()), fieldName)
                            .addModifiers(Modifier.VOLATILE)
                            .addModifiers(Modifier.PRIVATE)
            );

            getMethodBuilder.addModifiers(Modifier.SYNCHRONIZED)
                    .beginControlFlow("if(this.instance == null)")
                    .addStatement("this.instance = createInstance(wiredTypes)")
                    .endControlFlow()
                    .addStatement("return instance");
        } else {
            getMethodBuilder.addStatement("return createInstance(wiredTypes)");
        }

        addMethod(getMethodBuilder);
        return this;
    }

    public IdentifiableProviderClassBuilder applyMethodBuilder(MethodConstructor methodConstructor) {
        methodConstructor.construct(wireInformation, classBuilder());

        return this;
    }

    public IdentifiableProviderClassBuilder identifyingAWiredSource() {
        return applyMethodBuilder(new CreateInstanceForWireMethodConstructor());
    }

    public IdentifiableProviderClassBuilder identifyingAPropertySource() {
        return applyMethodBuilder(new CreateInstanceForPropertySourceMethodConstructor());
    }
}
