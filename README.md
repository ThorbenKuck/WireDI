
# SimpleDI

This "framework" is very simple and designed with Google Guice in mind, allowing for DI and IOC.

This project (even though released on maven central) is not meant to be used right now. Ist is more of a learning resources, to understand the difference between static and dynamic meta programming in Java.

## How does this framework work?

Easy. Create multiple classes, annotate them with @Wire and fetch a needed Instance using a `WiredTypes` class. The underlying annotation processor will generate classes, that handle dependency injection.

A little example. Let's assume, we want to have an interface, which looks like this:

```java
public interface Dependency {
    //....
}
```

with a package private implementation like this:

```java
class DependencyImpl implements Dependency {
    //....
}
```

Now we want to include this `Dependency` into a class called `Application` and do something with it. This class looks like this:

```java
public class Application {

    @Inject
    private Dependency dependency;

    public void start() {
        //....
    }
}
```

All we have to do, to make this work, is two little things. First, mark the `DependencyImpl` class as `Wired` to the `Dependency` interface, which is done with one annotation like this:

```java
@Wire(to = Dependency.class)
class DependencyImpl implements Dependency {
...
```

This will generate some factory like classes for us. Those classes are written into a service file, which will be read by the `WiredTypes` class. Now, to use our Application class, we only have to instantiate it like this:

```java
WiredTypes wiredTypes = new WiredTypes();
wiredTypes.load();

Application application = wiredTypes.getInstance(Application.class);
application.start();
```
