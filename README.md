# WireDI

What started as a proof of concept (originally named SimpleDI) to show, that dependency injection could be done at compile time, with not features left behind, grew up to become a very powerful framework, providing a lot of functionality (including aspect oriented proxies), all at runtime.

But let's start at the important questions:

## What does this framework do exactly?

In its core, this framework enables dependency injection. You add one annotation to a class you want to be wired by this framework, namely @Wire like this:

```java
@Wire
class YourClass {
    // Fields, methods, constructors etc.
}
```

and then you can extract the class with all wired classes like this:

```java
WiredTypes wiredTypes = new WiredTypes();
YourClass instance = wiredTypes.getInstance(YourClass.class);
```

All classes which you want to connect in this fashion have to be annotated with @Wire.

### So, how does this framework differ from other frameworks?

This framework does not work as other frameworks. It does work at compile time. Multiple annotation processors pick up different annotations and create certain other instances of suppliers.

### Ah, so it is one of those frameworks...

Yes and no. It does a lot of work at compile time, but not completely. Instead, it extracts the reflection heavy operations and does them at compile time. After compile time, it neatly snuggles into a more traditional framework. You can add custom instances, custom Aspects and other relevant informations all at runtime.

It is not recommended, but you can certainly do that. This allows us, to fit it into other frameworks.

## Installation

You require hooking up one annotation processor and the api, as well as the core api. You can do that simply, by adding this to pom.xml

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.github.thorbenkuck</groupId>
            <artifactId>simple-di-bootstrap</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

this introduces the annotation processor and dependency management. Afterwards, you can import all requirements by adding this dependency:

```xml
<dependencies>
    <dependency>
        <groupId>com.github.thorbenkuck</groupId>
        <artifactId>di</artifactId>
    </dependency>
</dependencies>
```

the `di` artifact contains everything you need to get done. You may only add the `di-annotations` artifact, but this would greatly limit you in functionality (especially since you now would have no access to the `WiredTypes` class :P).

## Basic usage

As you normally would do with any other DI framework, you can use it like this:

```java
@Wire
class A {
    private final B b;
    
    public A(B b) {
        this.b = b;
    }
}

@Wire
class B {}

public class Main {
    public static void main(String[] args)  {
        WiredTypes wiredTypes = new WiredTypes();
        B b = wiredTypes.getInstance(B.class);
    }
}
```

As you run this code, a lot happens behind the curtains. The annotation processor generates instance of `IdentifiableProvider` classes.

### Setter injection

### Field injection

## Aspects

## Properties

### Compile time bindings

## Property Bindings