package com.wiredi.runtime.types.converter;

import com.wiredi.runtime.types.AbstractTypeConverter;
import com.wiredi.runtime.types.TypeConverter;
import org.jetbrains.annotations.NotNull;

public class CharSequenceTypeConverter extends AbstractTypeConverter<CharSequence> {

    public static final CharSequenceTypeConverter INSTANCE = new CharSequenceTypeConverter();

    public CharSequenceTypeConverter() {
        super(CharSequence.class);
    }

    @Override
    protected void setup() {
        register(String.class, sequence -> sequence);
    }
}
