# Type Conversion

This library contains an abstraction for type conversion.

Type conversion here refers to injective projections of one type to another type.
A single type converter describes how to convert from type a to type b.

Type conversion is a simple form of conversion.
It has no concept of error handling or media types, it is just for converting between static types.
If you need more complex mechanisms, you can use deserialization/serialization in the messaging library.

A converter looks something like this:

```java
class CustomByteArrayTypeConverter implements TypeConverter<byte[]> {
    public @Nullable byte[] convert(@NotNull Object o) {
        if (o instanceof String s) {
            return s.getBytes();
        } else if (o instanceof byte[] b) {
            return b;
        } else {
            return null;
        }
    }
}
```

This example TypeConverter is converting from String to byteArray.
You could add other cases if you'd like.
However, this can become quite cumbersome.
To make your life easier, you can use the AbstractTypeConverter:

```java
public class CustomByteArrayTypeConverter extends AbstractTypeConverter<byte[]> {
    public ByteArrayTypeConverter() {
        super(byte[].class);
    }

    @Override
    protected void setup() {
        register(String.class, String::getBytes);
        register(byte[].class, b -> b);
    }
}
```

To use type conversions, you use the `com.wiredi.runtime.types.TypeMapper` object:
After instantiating the `TypeMapper`, you can set individual `TypeConverter` instances.
For example, like this:

```java
import com.wiredi.runtime.types.TypeMapper;

TypeMapper typeMapper = new TypeMapper();
typeMapper.setTypeConverter(new CustomByteArrayTypeConverter());
byte[] converted = typeMapper.convert("Hello Type Conversion", byte[].class);
```

## WireDI Integration

WireDI maintains a TypeMapper in the Environment.
It can be configured via `Environment#typeMapper` and is passed to the Environments TypeProperties.