package com.wiredi.runtime.types.converter;

import com.wiredi.runtime.types.TypeConverterBase;

/**
 * Converts values to {@link CharSequence}.
 *
 * This converter primarily adapts String inputs and related text types into a CharSequence. It is
 * part of the default {@link com.wiredi.runtime.types.TypeMapper} preconfiguration and provided as
 * a stateless singleton via {@link #INSTANCE}.
 */
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
