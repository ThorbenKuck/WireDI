
# Runtime Libraries

A comprehensive collection of standalone utility libraries that form the foundation of the WireDI ecosystem while being completely independent and usable on their own. Each library is designed with a specific purpose, following SOLID principles and maintaining high cohesion.

## Overview

These libraries are carefully crafted to be:
- **Standalone**: Use them with or without WireDI
- **Lightweight**: Minimal dependencies and small footprint
- **Focused**: Each library serves a specific purpose
- **Integration-ready**: Built with extensibility in mind

## Library Modules

### Core Utilities

#### Lang Module
Core language extensions and utilities for enhanced Java development
- Extended reflection capabilities
- Common language patterns
- Utility classes for common operations
- Enhanced exception handling

#### Collections Module
Specialized collection implementations and utilities
- Thread-safe collection types
- Performance-optimized collections
- Specialized data structures
- Collection transformation utilities

#### Type Converter
Comprehensive type conversion framework
- Built-in converters for common types
- Extensible conversion system
- Support for custom type conversions
- Format-specific converters

#### Value Module
Value handling and manipulation utilities
- Safe null handling
- Optional value processing
- Value validation
- Type-safe value containers

#### Properties Module
Advanced property management system
- Configuration file handling
- Environment variable integration
- Property encryption support
- Dynamic property updates

#### Resources Module
Resource management and loading utilities
- Classpath resource handling
- File system operations
- Resource pooling
- Resource cleanup management

### Concurrent and Async Operations

#### Async Module
Comprehensive asynchronous programming support
- Enhanced Future implementations
- Async operation composition
- Thread pool management
- Async operation monitoring

#### Messaging Module
Enterprise-grade messaging infrastructure
- Event publishing and subscription
- Message routing
- Channel management
- Message transformation

#### Retry Module
Sophisticated retry mechanism implementation
- Configurable retry policies
- Backoff strategies
- Failure handling
- Circuit breaker pattern

### Security and Monitoring

#### Security Module
Security utilities and integrations
- Authentication helpers
- Authorization utilities
- Cryptography tools
- Security context management

#### Logging Module
Advanced logging infrastructure
- Structured logging support
- Log routing
- Performance logging
- Log aggregation utilities

#### Metrics Module
Comprehensive metrics collection system
- Performance metrics
- Business metrics
- Metric aggregation
- Reporting utilities

#### Cache Module
Advanced caching infrastructure
- Multiple cache strategies
- Cache eviction policies
- Distributed caching support
- Cache synchronization

### Time Management

#### Time Module
Time handling and scheduling utilities
- Time zone management
- Duration calculations
- Scheduling utilities
- Time formatting

### Development Support

#### Test Utils
Comprehensive testing support
- Test data generators
- Assertion utilities
- Mock helpers
- Performance testing tools

## Integration with WireDI

While these libraries are designed to work independently, WireDI provides native integration modules that seamlessly incorporate these libraries into the IOC container. The integrations are available in the `integrations/` directory of the WireDI project.

### Available Integrations

#### Cache Integration
- Automatically registers cache managers and providers in the IOC container
- Provides dependency injection for cache configurations
- Enables declarative caching through annotations
- Manages cache lifecycle within the container

#### Logging Integration
- Configures logging infrastructure through container configuration
- Provides injectable logging facilities
- Manages logging contexts within the container scope
- Supports automatic logger injection

#### Messaging Integration
- Registers message handlers and processors automatically
- Provides container-managed message routing
- Enables declarative message handling
- Manages message broker connections through the container

#### Metrics Integration
- Automatically registers metric collectors
- Provides container-managed metric gathering
- Enables metric tracking through annotations
- Manages metric reporting lifecycle

#### Security Integration
- Integrates security providers with the container
- Manages security contexts
- Provides declarative security through annotations
- Handles security configuration through container

#### Retry Integration
- Enables declarative retry policies through the container
- Manages retry contexts
- Provides container-managed retry execution
- Configures retry strategies through container configuration

### Using Libraries with Integrations

You can use these libraries in three ways:

1. **Standalone Usage**
   ```java
   // Direct usage of the library
   RetryPolicy policy = RetryPolicy.builder()
       .withMaxAttempts(3)
       .build();
   ```

2. **With WireDI Integration**
   ```java
   // Let the container manage the retry policy
   @Retryable(maxAttempts = 3)
   public void operationWithRetry() {
       // Method implementation
   }
   ```

3. **Hybrid Approach**
    - Use standalone features where container integration isn't needed
    - Leverage container integration for more complex scenarios

### Integration Benefits

- **Automatic Configuration**: Libraries are automatically configured based on container settings
- **Lifecycle Management**: Component lifecycles are managed by the container
- **Dependency Injection**: Native support for injecting library components
- **Declarative Usage**: Annotation-based configuration and usage
- **Context Awareness**: Integration with container's context and scope management
