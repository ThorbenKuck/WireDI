package com.wiredi.compiler.tests.elements;

import javax.lang.model.element.Name;

/**
 * Mock implementation of Name for testing purposes.
 */
public class MockName implements Name {
    private final String name;

    public MockName(String name) {
        this.name = name;
    }

    @Override
    public boolean contentEquals(CharSequence cs) {
        return name.contentEquals(cs);
    }

    @Override
    public int length() {
        return name.length();
    }

    @Override
    public char charAt(int index) {
        return name.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return name.subSequence(start, end);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof Name) {
            return contentEquals((Name) obj);
        }
        if (obj instanceof CharSequence) {
            return name.contentEquals((CharSequence) obj);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}