package com.wiredi.properties;

import com.wiredi.annotations.properties.PropertyBinding;
import java.util.List;

@PropertyBinding(prefix = "test.properties")
public class PropertyBindingExample {
    private final String string;
    private final int integer;
    private final Nested nested;
//    private final List<Nested> furtherNested;
    private final PropertyBindingEnum enumValue;
    private PropertyBindingEnum setterEnumValue;

    public PropertyBindingExample(
            String string,
            int integer,
            Nested nested,
//            List<Nested> furtherNested,
            PropertyBindingEnum enumValue
    ){
        this.string = string;
        this.integer = integer;
        this.nested = nested;
//        this.furtherNested = furtherNested;
        this.enumValue = enumValue;
    }

//    public List<Nested> getFurtherNested() {
//        return furtherNested;
//    }

    public void setSetterEnumValue(PropertyBindingEnum setterEnumValue) {
        this.setterEnumValue = setterEnumValue;
    }

    public PropertyBindingEnum getSetterEnumValue() {
        return this.setterEnumValue;
    }

    public PropertyBindingEnum getEnumValue() {
        return enumValue;
    }

    public String getString() {
        return string;
    }

    public int getInteger() {
        return integer;
    }

    public Nested getNested() {
        return nested;
    }

    public static class Nested {
        private final String string;
        private final int integer;

        public Nested(String string, int integer) {
            this.string = string;
            this.integer = integer;
        }

        public String getString() {
            return string;
        }

        public int getInteger() {
            return integer;
        }
    }
}
