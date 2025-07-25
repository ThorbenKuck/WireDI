package com.wiredi.compiler.domain;

import com.squareup.javapoet.CodeBlock;
import com.wiredi.runtime.domain.annotations.AnnotationMetadata;

import javax.lang.model.element.AnnotationMirror;

public class AnnotationMetaDataSpec {

    public static CodeBlock initializer(AnnotationMirror mirror) {
        return initializer(AnnotationMetadata.of(mirror));
    }

    public static CodeBlock initializer(AnnotationMetadata metaData) {
        if (metaData.isEmpty()) {
            return CodeBlock.builder()
                    .add("$T.empty($S)", AnnotationMetadata.class, metaData.className())
                    .build();
        }

        CodeBlock.Builder metaDataInitializer = CodeBlock.builder()
                .add("$T.builder($S)", AnnotationMetadata.class, metaData.className())
                .indent();
        metaData.forEach((field, value) -> appendField(metaDataInitializer, field, value));
        metaDataInitializer.add("\n.build()").unindent();
        return metaDataInitializer.build();
    }

    private static void appendField(CodeBlock.Builder rootBlock, String field, Object value) {
        if (value.getClass().isMemberClass()
                || value.getClass().isLocalClass()
                || value.getClass().isAnonymousClass()
                || value.getClass().isAnnotation()) {
            rootBlock.add("\n.withField($S, $T.class)", field, value);
        } else if (value.getClass().equals(String.class)) {
            rootBlock.add("\n.withField($S, $S)", field, value);
        } else {
            rootBlock.add("\n.withField($S, $L)", field, value);
        }
    }
}
