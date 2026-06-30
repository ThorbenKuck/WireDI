# Runtime Environment

This module provides the core runtime building blocks of WireDI: the WireContainer, the Environment, and the application bootstrap utilities around WiredApplication. This document is written for developers who want to use WireDI effectively in real-world applications. It focuses on usability, lifecycle, and practical code examples.

If you are new to WireDI, start here.

- WireContainer is the IoC container you work with at runtime.
- Environment manages properties, resources, type conversion, and profiles.
- WiredApplication is a convenience bootstrap that wires both together, adds diagnostics and lifecycle management, and gives you an application handle.


## Quick start

Pick one of these entry points depending on your use case.

- Minimal container:

```java
import com.wiredi.runtime.WireContainer;

public class Main {
    public static void main(String[] args) {
        WireContainer container = WireContainer.open(); // fully configured and loaded
        MyService my = container.get(MyService.class);
        my.run();
    }
}
```

- Minimal application with lifecycle management:

```java
import com.wiredi.runtime.WiredApplication;

public class Main {
    public static void main(String[] args) {
        WiredApplication.run(); // runs until JVM shutdown (or programmatically stopped)
    }
}
```

- Custom configuration on startup:

```java
import com.wiredi.runtime.WiredApplication;
import com.wiredi.runtime.WireContainer;

public class Main {
    public static void main(String[] args) {
        WiredApplication.run(container -> {
            // Register custom instances or providers before the container loads
            container.announce(new MyManuallyWiredService());
        });
    }
}
```

- Launch, keep a handle, and shut down programmatically:

```java
import com.wiredi.runtime.WiredApplication;
import com.wiredi.runtime.WiredApplicationInstance;

public class Main {
    public static void main(String[] args) {
        WiredApplicationInstance app = WiredApplication.start(container -> {
            // optional customizations
        });

        // ... do work ...

        app.shutdown();
    }
}
```


## WireContainer: your runtime IoC container

WireContainer is the core runtime container. It holds providers generated at compile time, plus any manual providers or instances you register at runtime.

Key ways to obtain a container:

- WireContainer.open(): build and load a fully configured container using a default Environment.
- WireContainer.open(env): same, but with your Environment instance.
- WireContainer.create(): build a not-yet-loaded container (you decide when/how to load).
- WireContainer.builder(): advanced, fluent configuration (then call load or build).

Typical usage:

```java
WireContainer container = WireContainer.open();
MyService a = container.get(MyService.class);
Optional<Other> maybe = container.tryGet(Other.class);
List<Plugin> all = container.getAll(Plugin.class);
Provider<Repository> repoProvider = container.getProvider(Repository.class);
```

Search with qualifiers:

```java
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.qualifier.QualifierType;

PaymentProcessor credit = container
    .search(TypeIdentifier.just(PaymentProcessor.class))
    .withQualifier(QualifierType.named("credit"))
    .find();
```

Announce manual instances or providers:

```java
// announce an already constructed instance
container.announce(new MetricsRegistry());

// announce a custom IdentifiableProvider
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.TypeIdentifier;

container.announce(IdentifiableProvider.singleton(
    new MetricsRegistry(),
    TypeIdentifier.just(MetricsRegistry.class)
));
```

Lifecycle operations on the container:

```java
// Load the container with custom behavior
container.load(ctx -> new WireContainer.LoadConfig()
    .initializeEagerBeans(true)   // create beans annotated/marked to be eager
    .synchronizeOnStates(true)    // wait for StateFull components to reach ready state
);

// Explicit lifecycle helpers (also driven by WiredApplication)
container.initializeEagerBeans();
container.synchronizeOnStates(Duration.ofSeconds(30));

// Clear everything when you are done
container.clear();
```

Useful collaborators you can access:

- environment(): the Environment bound to this container
- onDemandInjector(): reflection-based, ad-hoc construction for advanced cases (prefer compile-time providers)
- startupDiagnostics(): capture timings and structure of the startup process

Note on OnDemandInjector: it uses reflection. Prefer the generated providers and IdentifiableProvider integrations for performance and safety. Use onDemandInjector only when you know you need dynamic wiring.


## Environment: configuration, properties, resources, profiles

Environment is the central service for configuration data. It is independent and reusable.

- Build a ready-to-use Environment:

```java
import com.wiredi.runtime.Environment;

Environment env = Environment.build(); // autoconfigures itself via ServiceLoader
```

- Or construct and autoconfigure later:

```java
Environment env = new Environment();
env.autoconfigure();
```

What autoconfiguration does (via ServiceLoader):

- Adds EnvironmentExpressionResolver implementations (for placeholder/expression resolution)
- Adds ResourceProtocolResolver implementations (resource loading like classpath:, file:, etc.)
- Adds PropertyFileTypeLoader implementations (e.g. .properties, .yaml if present)
- Applies EnvironmentConfiguration implementations (see order/precedence below)

Property sources and precedence

EnvironmentConfiguration implementations are applied in order.
Later sources override earlier ones for the same keys.
Built-ins load in this order (first to last):

1) application.* files (ApplicationPropertiesEnvironmentConfiguration)
2) additional property files (AdditionalPropertiesEnvironmentConfiguration)
3) application-<profile>.* files for each active profile (ProfilePropertiesEnvironmentConfiguration)
4) OS environment variables (OSEnvironmentConfiguration) — normalized keys (e.g., APP_NAME -> app.name)
5) JVM system properties (SystemPropertiesEnvironmentConfiguration)

This yields the following precedence (highest wins):

- System properties
- OS environment variables
- Profile-specific properties
- Additional properties
- Default properties (application.*)

Code examples

Reading properties (typed and with defaults):

```java
import com.wiredi.runtime.properties.Key;

String appName = env.getProperty(Key.just("app.name"), "MyApp");
int port = env.getProperty(Key.just("server.port"), Integer.class, 8080);
Duration timeout = env.getProperty(Key.just("client.timeout"), Duration.class, Duration.ofSeconds(5));
List<String> hosts = env.getAllProperties(Key.just("cluster.hosts"), String.class);
```

Resolving placeholders and lists:

```java
String raw = "Hello ${user.name}, home=${env:HOME}"; // exact expression syntax depends on available resolvers
String resolved = env.resolve(raw);
List<String> profiles = env.resolveList("active.profiles");
```

Loading resources and properties at runtime:

```java
var resource = env.loadResource("classpath:application.properties");
if (resource.exists()) {
    env.appendPropertiesFrom(resource);
}

// or load a new TypedProperties and inspect
var props = env.loadProperties("file:/etc/myapp/custom.properties");
```

Profiles

- Active profiles live under the key active.profiles.
- ApplicationPropertiesEnvironmentConfiguration ensures that an active profile exists. If none is provided, it uses com.wiredi.environment.default-profile (default: "default").
- ProfilePropertiesEnvironmentConfiguration then loads application-<profile>.* for each active profile and merges them.

Example: enable a profile before container startup

```java
import com.wiredi.runtime.properties.Key;

Environment env = Environment.build();
env.setProperty(Key.just("active.profiles"), "prod");
WiredApplication.run(container -> {}, env -> {}); // or pass env into WiredApplication.start(env, ...)
```

Note: When using WiredApplication, you can pass your Environment instance to start(Environment, Consumer<WireContainer>). Otherwise WiredApplication builds a default Environment internally.


## Lifecycle: startup and shutdown

WireDI has a predictable, explicit lifecycle. You can drive it yourself via WireContainer or use WiredApplication to orchestrate it.

Container-driven lifecycle (manual):

```java
Environment env = Environment.build();
WireContainer container = WireContainer.create(env);

// Register custom things before load
container.announce(new CustomInstance());

// Load with the desired behavior
container.load(ctx -> new WireContainer.LoadConfig()
    .initializeEagerBeans(true)
    .synchronizeOnStates(true)
);

// Use your beans
var service = container.get(MyService.class);

// Tear down when done
container.clear();
```

Application-driven lifecycle (recommended for apps):

WiredApplicationInstance.start() performs these steps:

1) environment.autoconfigure()
2) Print a banner and the active profiles
3) If the container is not loaded, wire banner into the container and load the container
   - During load, Environment is enriched with any bean-provided resolvers/loaders/configurations
   - Load behavior is controlled by properties:
     - wiredi.startup.load-eager-instances (default: true)
     - wiredi.startup.await-states (default: true)
     - wiredi.startup.await-states-timeout (Duration, optional)
4) Register a JVM shutdown hook
5) Mark application as running
6) Optionally print startup diagnostics when wiredi.startup.print-diagnostics=true

Shutdown (via app.shutdown() or JVM shutdown):

- Dismantle all StateFull beans
- Tear down all Disposable beans
- Clear the Environment
- Notify callbacks
- Clear the WireContainer
- Remove the shutdown hook (if not on the shutdown thread)

Example: a Disposable bean

```java
import com.wiredi.runtime.domain.Disposable;
import com.wiredi.annotations.Wire;

@Wire
class ConnectionPool implements Disposable {
    @Override
    public void tearDown(WireContainer origin) {
        // close all connections
    }
}
```

Example: a StateFull bean (simplified)

```java
import com.wiredi.runtime.async.StateFull;
import com.wiredi.annotations.Wire;

@Wire
class Cache implements StateFull {
    // Implement initialize/dismantle hooks according to the StateFull contract
}
```

Diagnostics

Set wiredi.startup.print-diagnostics=true to log a readable timing tree of the container startup. Useful to understand what takes time during boot.


## WiredApplication: expanding WireContainer into an application

WiredApplication wraps a WireContainer and an Environment into an application concept with a clear lifecycle and a convenient API.

What it does for you:

- Builds or accepts an Environment
- Builds a WireContainer with that Environment
- Registers a special ShutdownListener to coordinate application termination
- Enriches the Environment with bean-provided resolvers and loaders before loading the container
- Loads the container with behavior controlled by properties (eager beans, state synchronization, timeout)
- Prints a banner and active profiles
- Installs a JVM shutdown hook and exposes awaitCompletion()
- Optionally prints startup diagnostics

APIs:

```java
// Block until the application terminates
WiredApplication.run();
WiredApplication.run(container -> { /* customize */ });

// Start and control the lifecycle yourself
WiredApplicationInstance instance = WiredApplication.start();
WiredApplicationInstance instance = WiredApplication.start(container -> { /* customize */ });
WiredApplicationInstance instance = WiredApplication.start(myEnvironment, container -> { /* customize */ });

instance.awaitCompletion(); // block the current thread until shutdown
instance.shutdown();        // request a graceful shutdown

WireContainer container = instance.wireContainer(); // access the underlying container
Environment env = instance.environment();          // access the Environment
```

Customizing before load

Use the Consumer<WireContainer> provided to start/run to announce custom instances or providers before the container loads. This is the right place to bridge legacy singletons, integrate third-party SDK instances, or override generated providers during tests.

Example: inject a test double

```java
WiredApplicationInstance app = WiredApplication.start(container -> {
    container.announce(new InMemoryEmailGateway());
});
```

Providing your own Environment

You may want to configure active profiles, additional property files, or toggle startup flags before boot. Pass an Environment to start:

```java
import static com.wiredi.runtime.PropertyKeys.*;
import com.wiredi.runtime.properties.Key;

Environment env = Environment.build();

env.setProperty(Key.just("active.profiles"), "dev,feature-x");
// Enable diagnostics and set a startup timeout
env.setProperty(PRINT_DIAGNOSTICS.getKey(), "true");
env.setProperty(AWAIT_STATES_TIMEOUT.getKey(), "PT30S");

WiredApplicationInstance app = WiredApplication.start(env, container -> {
    // announce custom instances/providers here
});
```


## Design highlights (short)

- Compile-time first, runtime flexible: providers are pre-generated, but you can still announce custom instances at runtime.
- Environment-first configuration: properties, profiles, and resources are handled uniformly and can be enriched by container-provided components.
- Clear lifecycle: explicit load, optional eager initialization, state synchronization, and predictable shutdown.
- Extensible: add EnvironmentConfiguration, ResourceProtocolResolver, PropertyFileTypeLoader, and EnvironmentExpressionResolver via ServiceLoader or as beans.


## Testing tips

- For unit tests, use WireContainer.open() and replace collaborators via container.announce(...).
- Use profiles to switch configurations for tests (e.g., active.profiles=test).
- Turn on diagnostics when debugging boot order or performance: wiredi.startup.print-diagnostics=true.
- In integration tests, start a WiredApplicationInstance and shut it down at the end:

```java
WiredApplicationInstance app = WiredApplication.start();
try {
    // run tests against app.wireContainer()
} finally {
    app.shutdown();
}
```


## Frequently used property keys

- wiredi.startup.load-eager-instances (boolean, default: true)
- wiredi.startup.await-states (boolean, default: true)
- wiredi.startup.await-states-timeout (Duration, e.g., PT30S)
- wiredi.startup.print-diagnostics (boolean, default: false)
- debug (boolean, default: false)
- active.profiles (comma-separated list)
- com.wiredi.environment.additional-properties (list of resource paths)
- com.wiredi.environment.default-profile (fallback profile name, default: "default")


## Related modules

- runtime-libraries: common runtime utilities used by the container
- compile-time: annotation processors that generate providers and metadata
- bundles: optional, pre-packaged bundles (e.g., logging) that register useful defaults
- integrations: optional modules that integrate WireDI with other libraries


## Appendix: manual builder usage

```java
Environment env = Environment.build();
WireContainer container = WireContainer.builder()
    .withEnvironment(env)
    // optional: customize scopes, initializer, diagnostics here through the builder
    .load();
```

If you need a not-yet-loaded container, call .build() instead and later use container.load(...) yourself.
