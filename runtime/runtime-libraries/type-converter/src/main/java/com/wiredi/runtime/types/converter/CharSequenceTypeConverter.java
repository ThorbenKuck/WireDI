package com.wiredi.runtime.types.converter;

import com.wiredi.runtime.types.TypeConverterBase;

public class CharSequenceTypeConverter extends TypeConverterBase<CharSequence> {

    public static final CharSequenceTypeConverter INSTANCE = new CharSequenceTypeConverter();

    public CharSequenceTypeConverter() {
        super(CharSequence.class);
    }

    @Override
    protected void setup() {
        register(String.class, sequence -> sequence);
    }
}
