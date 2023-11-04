package com.wiredi.compiler.domain;

import com.squareup.javapoet.CodeBlock;
import com.wiredi.domain.AnnotationMetaData;

import javax.lang.model.element.AnnotationMirror;

public class AnnotationMetaDataSpec {

    public static CodeBlock initializer(AnnotationMirror mirror) {
        AnnotationMetaData metaData = AnnotationMetaData.of(mirror);

        if (metaData.isEmpty()) {
            return CodeBlock.builder()
                    .add("$T.empty($S)", AnnotationMetaData.class, mirror.getAnnotationType())
                    .build();
        }

        CodeBlock.Builder metaDataInitializer = CodeBlock.builder()
                .add("$T.newInstance($S)", AnnotationMetaData.class, mirror.getAnnotationType())
                .indent();
        metaData.forEach((field, value) -> metaDataInitializer.add("\n.withField($S, $L)", field, value));
        metaDataInitializer.add("\n.build()").unindent();
        return metaDataInitializer.build();
    }

    public static CodeBlock initializer(AnnotationMetaData metaData) {
        if (metaData.isEmpty()) {
            return CodeBlock.builder()
                    .add("$T.empty($S)", AnnotationMetaData.class, metaData.className())
                    .build();
        }

        CodeBlock.Builder metaDataInitializer = CodeBlock.builder()
                .add("$T.newInstance($S)", AnnotationMetaData.class, metaData.className())
                .indent();
        metaData.forEach((field, value) -> {
            if (value.getClass().isMemberClass()
                    || value.getClass().isLocalClass()
                    || value.getClass().isAnonymousClass()) {
                metaDataInitializer.add("\n.withField($S, $T.class)", field, value);
            } else if (value.getClass().equals(String.class)) {
                metaDataInitializer.add("\n.withField($S, $S)", field, value);
            } else {
                metaDataInitializer.add("\n.withField($S, $L)", field, value);
            }
        });
        metaDataInitializer.add("\n.build()").unindent();
        return metaDataInitializer.build();
    }

}
