# Values

Values are wrapper classes for fields.
A Value instance is abstracted through an interface and then allows for async, lazy or fixed setting of field values.

Some examples are:

```java
import com.wiredi.runtime.values.Value;

class MyClass {
    // Value will just contain the string "Hello World"
    private final Value<String> exampleOne = Value.just("Hello World");

    // Value will be empty at the beginning, but filled lazily with "Hello World"
    private final Value<String> exampleTwo = Value.lazy(() -> "Hello World");

    // Value will be empty at the beginning, but filled in a separate thread with "Hello World"
    private final Value<String> exampleThree = Value.async(() -> "Hello World");

    // Value will just contain the string "Hello World", but read/write operations will be synchronized
    private final Value<String> exampleFour = Value.synchronize("Hello World");

    public void example() {
        String value = exampleOne.get();
        exampleOne.set("Not Hello World");
    }
}
```

Additionally, there are functions to read the value smartly, like:

```java
import com.wiredi.runtime.values.Value;

class MyClass {
    // Value will just contain the string "Hello World"
    private final Value<String> value = Value.just("Hello World");

    public void example() {
        value.ifPresent(System.out::println)
                .orElse(() -> System.out.println("IMPOSSIBLE!"));
        value.ifEmpty(() -> System.out.println("IMPOSSIBLE!"));
        String willBeHelloWorld = value.getOrSet(() -> "Not Hello World");
    }
}
```