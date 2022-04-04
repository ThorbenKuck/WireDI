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
YourClass instance = wiredTypes.get(YourClass.class);
```

All classes which you want to connect in this fashion have to be annotated with @Wire.

### So, how does this framework differ from other frameworks?

This framework does not work as other frameworks. It does work at compile time. Multiple annotation processors pick up different annotations and create certain other instances of suppliers.

### Ah, so it is one of those frameworks...

Yes and no. It does a lot of work at compile time, but not completely. Instead, it extracts the reflection heavy operations and does them at compile time. After compile time, it neatly snuggles into a more traditional framework. You can add custom instances, custom Aspects and other relevant informations all at runtime.

It is not recommended, but you can certainly do that. And this allows us to enter a whole new dimension. We can fit this framework with relative ease into other, already existing frameworks, combining the power of annotation processors and the already existing ecosystem of runtime DI and IOC.

We can go even as far as declaring aspects at runtime, even though the proxies are build at compile time. But let us start from the beginning:

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
        <artifactId>wire-di</artifactId>
    </dependency>
</dependencies>
```

the `di` artifact contains everything you need to get done. You may only add the `di-annotations` artifact, but this would greatly limit you in functionality (especially since you now would have no access to the central `WiredTypes` class :P).

## Basic usage

The central annotation to mark classes as "I want to be able to be handled for di" is called `Wire`. This annotation marks a class as "Injection enabled", allowing the framework to inject it into other classes and other classes into it.

A very simple example would look like this:

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
        A a = wiredTypes.get(A.class);
        // Do fancy stuff
    }
}
```

In this example right here, we have two classes, A and B and A has a dependency to B. Since both classes are marked with `@Wire`, the framework can identify these classes and inject them safely into each other.

As you run this code, a lot happens behind the curtains. The annotation processor generates instance of `IdentifiableProvider` classes, which you can even see in the compiled sources. These providers hold a sum of static information about this class, that are then used at runtime; Including the template as to how this class is created.

#### And why does everything need the @Wire annotation

You are right. Up to a certain point, it would be very easy to remove it. We could just say "the root class needs to have this annotation" and the rest might just be instantiated.

By adding the annotation requirement, we explicitly allow intersection of created instance and to control the lifecycle of those, even though it would be faster when we were doing it correctly.

Far in the future, in an utopia (or dystopia?) where everything would happen at compile time, we could think about dropping this requirement, but for now we won't. This requirement allows us the interoperability with all the existing frameworks

### Setter injection

### Field injection

## Aspects

## Properties

### Compile time bindings

## Property Bindings