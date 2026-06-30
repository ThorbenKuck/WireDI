package com.wiredi.runtime.transactions;

import jakarta.inject.Qualifier;

import java.lang.annotation.*;

@Documented
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface TransactionType {
    Class<?> value();
}
