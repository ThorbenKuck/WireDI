
# WireDI Integrations

This module provides native integrations between WireDI's IOC container and the standalone runtime libraries. These integrations enable seamless usage of runtime libraries within the container context, offering automatic configuration, dependency injection, and lifecycle management.

## Available Integrations

### Cache Integration
Integrates the runtime caching library with the IOC container.

**Features:**
- Automatic cache manager configuration and injection
- Support for multiple cache providers
- Declarative caching through annotations
- Cache lifecycle management
- Transaction-aware caching
- Custom cache key generation

### Jackson Integration
Provides JSON serialization and deserialization capabilities within the container.

**Features:**
- Automatic ObjectMapper configuration
- Custom serializer/deserializer registration
- Type-aware JSON conversion
- Integration with container's type system
- Support for modular configurations

### Logging Integration
Connects the logging runtime library with the container's infrastructure.

**Features:**
- Automatic logger injection
- Context-aware logging
- Structured logging support
- Log level management
- MDC integration
- Custom log formatter support

### Messaging Integration
Enables message-based communication within the container context.

**Features:**
- Automatic message handler registration
- Support for multiple message brokers
- Message routing and transformation
- Event publishing and subscription
- Message persistence
- Dead letter handling

### Metrics Integration
Provides container-managed metrics collection and reporting.

**Features:**
- Automatic metric collection
- Multiple reporter support
- Custom metric types
- Aggregation support
- Dimensional metrics
- Export capabilities

### Retry Integration
Implements retry capabilities within the container context.

**Features:**
- Declarative retry policies
- Multiple backoff strategies
- Circuit breaker pattern
- Retry listeners
- Custom retry templates
- Fallback handling

### Security Integration
Provides security infrastructure integration with the container.

**Features:**
- Authentication provider integration
- Authorization management
- Security context propagation
- Method-level security
- Role-based access control
- Custom security rules

## Usage

### Basic Setup

Add the desired integration to your project

```xml
<dependency>
    <groupId>com.wiredi</groupId>
    <artifactId>the-artifact-id</artifactId>
    <version>${wiredi.version}</version>
</dependency>
```

No need to add the runtime-library, as the integration always bundles it.
If, for example, you add the cache-integration, it bundles the cache runtime library.

And that's it really.