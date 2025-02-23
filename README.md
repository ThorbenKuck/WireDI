# WireDI

[![Java CI with Maven](https://github.com/ThorbenKuck/WireDI/actions/workflows/maven.yml/badge.svg)](https://github.com/ThorbenKuck/WireDI/actions/workflows/maven.yml)

![header](.img/header.png)

## Links:

- [Documentation](https://docs.thorbenkuck.de/wiredi)
- [Maven-Central](https://repo1.maven.org/maven2/com/github/thorbenkuck/wire-di-bootstrap/)

This project started out as a proof of concept (originally named SimpleDI), to show that dependency injection (or more precisely IOC) could be done at compile time, while keeping the feature set from reflections.
This POC grew up to become WireDI, a compile time oriented framework, that still allows for runtime interactions.

It is full of all the features that you might know from existing IOC frameworks, but integrates the best of runtime
reflection based operations into compile time, reducing the startup overhead.
It does this and still allows for runtime adjustments.

But let's start at the important questions:

## What does this framework do exactly?

In its core, this framework is an IOC container.
You add one annotation to a class you want to be wired by this framework (namely `@Wire`) like this:

```java
@Wire
class YourClass {
    // Fields, methods, constructors etc.
}
```

and then you can extract the class with all wired classes like this:

```java
WireRepository wireRepository = WireRepository.open();
MyClass instance = wireRepository.get(MyClass.class);
```

All classes that you want to connect in this fashion have to be annotated with @Wire.

### Yeah, I have seen this before... So, how does this framework differ from other frameworks?

Instead of retrieving annotated classes, constructing AST and proxies at startup, WireDI pre-compiles this information
at compile time.
Multiple annotation processors pick up different annotations and create certain other instances of suppliers.

This means, that as you call `WireRepository.open()`, all proxies already exist, all dependency requirements are analyzed and all qualifiers are correctly set.

Though precompiled, the data are not static.
You can modify, change, remove or even manipulate the process.
In total, it pre-analyzes the AST, but does not generate static IOC.

### Ah, so it is one of those frameworks...

Yes and no.
It does a lot of work at compile time, but as a state, not completely.
Instead, it extracts the reflection heavy operations and does them at compile time.
After compile time, it neatly snuggles into a more traditional framework.
You can add custom instances, custom Aspects and other relevant information all at runtime.

It is not recommended, but you can certainly use it to do everything at runtime.
And the combination of compile time and run time support allows us to enter a whole new dimension.
We can integrate other IOC frameworks into this once with relative ease.
Already existing frameworks, can benefit from the power of annotation processors and the already existing ecosystem of
runtime DI and IOC.

We can go even as far as declaring aspects at runtime, even though the proxies are build at compile time.
But let us start from the beginning:

## Quick Start

To use WireDi, you need two things: The `runtime environment` and the `processors`.

#### 1) Runtime Environment

Add the runtime environment like this:

```xml
<dependency>
    <groupId>com.wiredi</groupId>
    <artifactId>runtime-environment</artifactId>
    <version>${revision}</version>
</dependency>
```

This dependency will introduce the requirements to use the IOC container `WireRepository`, like this:

```java
import com.wiredi.runtime.WireRepository;

public class Example {
    public static void main(String[] args) {
        WireRepository iocContainer = WireRepository.open();
    }
}
```

Additionally, to make classes available to the IOC container,
this dependency also introduces the `@Wire` annotation, that you use like this:

```java
import com.wiredi.annotations.Wire;

@Wire
public class MyDependency {
    // Members
}
```

#### 2) Processors

The runtime environment itself will allow you to use the IOC container.
To fill it, you will additionally need the processors.
They need to be accessible to the compiler; There is no need to hold them in the class path for the runtime.

You can include them using the maven-compiler-plugin:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.8.1</version>
    <configuration>
        <parameters>true</parameters>
        <annotationProcessorPaths>
            <annotationProcessorPath>
                <groupId>com.wiredi</groupId>
                <artifactId>processors</artifactId>
                <version>${wiredi.version}</version>
            </annotationProcessorPath>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

You require hooking up one annotation processor and the api, as well as the core api.
You can do that simply, by adding this parent to your pom.xml

```xml
<parent>
    <groupId>com.wiredi</groupId>
    <artifactId>bootstrap</artifactId>
    <version>${revision}</version>
    <relativePath/> <!-- Always look up parent from repository -->
</parent>
```

This parent introduces the wire-di annotation processor and wire-di-runtime-environment, which allows you at run time
to utilize the data generated at compile time.

For other forms of introducing this library to your application, see the [usage section of the documentation](https://docs.thorbenkuck.de/wiredi/#/usage/).

## Basic usage

The central annotation to mark classes as "I want to be able to be handled for di" is called `Wire`.
This annotation marks a class as "Injection enabled," allowing the framework to inject it into other classes and other
classes into it.

A basic example would look like this:

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
        WireRepository wireRepository = WireRepository.open();
        A a = wireRepository.get(A.class);
        // Do fancy stuff
    }
}
```

In this example right here, we have two classes, A and B and A has a dependency to B.
Since both classes are marked with`@Wire`, the framework can identify these classes and inject them safely into each other.

As you run this code, a lot happens behind the curtains.
The annotation processor generates instance of `IdentifiableProvider` classes, which you can even see in the compiled sources.
These providers hold a sum of static information about this class, that are then used at runtime;
Including the template as to how this class is created.

## Examples

If you'd like to see a few examples on how to use WireDI, have a look at the [examples](examples).
This project contains concrete examples for WireDI.

## Properties

Order of Properties:

- default properties (application.properties)
- additional properties (key=load.additional-properties)
- profile properties (application-<profile>.properties)
- OS Environment Variables
- System Properties

# Project Structure

The project is mainly split into two modules: [runtime](runtime) and [compile time](compile-time).