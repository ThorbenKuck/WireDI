package com.wiredi.compiler.domain.entities.methods.identifiableprovider;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.wiredi.annotations.Order;
import com.wiredi.compiler.domain.Annotations;
import com.wiredi.compiler.domain.ClassEntity;
import com.wiredi.compiler.domain.entities.methods.StandaloneMethodFactory;
import com.wiredi.compiler.logger.Logger;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

public class OrderMethod implements StandaloneMethodFactory {

    private static final Logger logger = Logger.get(OrderMethod.class);
    private final CodeBlock initializer;

    public OrderMethod(Order annotation, Types types) {
        this(getOrder(annotation, types).estimateOr(annotation.value()));
    }

    public OrderMethod(int order) {
        this(CodeBlock.of("$L", order));
    }

    public OrderMethod(CodeBlock codeBlock) {
        this.initializer = codeBlock;
    }

    @Override
    public void append(MethodSpec.Builder builder, ClassEntity<?> entity) {
        builder.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .returns(TypeName.INT)
                .addAnnotation(Override.class)
                .addStatement("return $L", initializer)
                .build();
    }

    private static OrderRange getOrder(Order annotation, Types types) {
        OrderRange orderRange = new OrderRange();
        TypeMirror before = Annotations.extractType(annotation, Order::before);
        TypeMirror after = Annotations.extractType(annotation, Order::after);

        if (isNotVoidType(before)) {
            Order beforeAnnotation = Annotations.getAnnotation(types.asElement(before), Order.class).orElse(null);
            logger.info("Determined before " + beforeAnnotation + " on " + before + "(" + before.getAnnotationMirrors() + ")");
            if (beforeAnnotation == null) {
                orderRange.before = Order.DEFAULT;
            } else {
                orderRange.before = getOrder(beforeAnnotation, types).estimateOr(beforeAnnotation.value());
            }
        }
        if (isNotVoidType(after)) {
            Order afterAnnotation = Annotations.getAnnotation(types.asElement(after), Order.class).orElse(null);
            logger.info("Determined after " + afterAnnotation);
            if (afterAnnotation == null) {
                orderRange.after = Order.DEFAULT;
            } else {
                orderRange.after = getOrder(afterAnnotation, types).estimateOr(afterAnnotation.value());
            }
        }

        return orderRange;
    }

    private static boolean isNotVoidType(TypeMirror typeMirror) {
        return !typeMirror.toString().equals(Void.class.getName());
    }

    @Override
    public String methodName() {
        return "getOrder";
    }

    static class OrderRange {
        private Integer before = null;
        private Integer after = null;

        // Before = 6
        // After = 4
        // 4 + ((6 - 4) / 2)
        // 4 + (2 / 2)
        // 4 + 1
        // 5
        public int estimateOr(int alternative) {
            if (before != null && after != null) {
                if (before <= (after + 1)) {
                    throw new IllegalStateException("Before should be less than after + 1, but before=" + before + " and after=" + after + ". To reliably put an order between to others, at least one free");
                }
                return after + Math.floorDiv(before - after, 2);
            }

            if (after != null) {
                return after + 1;
            }
            if (before != null) {
                return before - 1;
            }

            return alternative;
        }
    }
}
