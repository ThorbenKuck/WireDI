# WireDI Cache Library

A flexible and type-safe caching abstraction for Java applications, part of the WireDI framework.

## Introduction

The WireDI Cache Library provides a generic caching abstraction that allows applications to store and retrieve data efficiently. It offers a consistent API for implementing various caching strategies while maintaining type safety and flexibility.

This library is designed to:
- Reduce redundant computations and database queries
- Improve application performance
- Provide a consistent way to work with cached data
- Integrate seamlessly with the WireDI dependency injection framework

## Key Features

- **Generic Caching Abstraction**: A clean, consistent API for working with cached data
- **Type-Safe Caching**: Generic type parameters ensure type safety at compile time
- **In-Memory Implementation**: Efficient in-memory cache with configurable eviction policies
- **Thread-Safe Cache Management**: Thread-safe access to caches with per-cache locking
- **Multiple Cache Support**: Maintain multiple caches with different purposes and configurations
- **Configurable Eviction Policies**: Support for both LRU (Least Recently Used) and LFU (Least Frequently Used) eviction
- **Flexible Cache Identification**: Identify caches by type parameters and custom identifiers
- **Dependency Injection Integration**: Seamless integration with WireDI's dependency injection

## Installation

Add the following dependency to your Maven project:

```xml
<dependency>
    <groupId>com.wiredi</groupId>
    <artifactId>cache</artifactId>
    <version>${wiredi.version}</version>
</dependency>
```

## Basic Usage

### Creating a Cache Manager

```java
// Create a cache manager with default configuration
CacheManager cacheManager = new InMemoryCacheManager();
```

### Getting or Creating a Cache

```java
// Get or create a cache for users
Cache<String, User> userCache = cacheManager.getCache(
    String.class, User.class, "userCache"
);

// Get or create a cache for products
Cache<Integer, Product> productCache = cacheManager.getCache(
    Integer.class, Product.class, "productCache"
);
```

### Storing and Retrieving Values

```java
// Store a value in the cache
User user = new User("john.doe", "John Doe");
userCache.put(user.getUsername(), user);

// Retrieve a value from the cache
Optional<User> cachedUser = userCache.get("john.doe");
if (cachedUser.isPresent()) {
    System.out.println("Found user: " + cachedUser.get().getName());
} else {
    System.out.println("User not found in cache");
}

// Retrieve with a default value supplier (computed and cached if not present)
User user = userCache.getOr("jane.doe", () -> {
    // This will only be called if the user is not in the cache
    return fetchUserFromDatabase("jane.doe");
});
```

### Invalidating Cache Entries

```java
// Remove a specific entry
userCache.invalidate("john.doe");

// Clear the entire cache
userCache.invalidate();
```

## Advanced Usage

### Custom Cache Configurations

```java
// Create a custom configuration
InMemoryCacheConfiguration config = InMemoryCacheConfiguration.builder()
    .withCapacity(200)
    .withReorderOnHit(true)  // Enable LRU behavior
    .withHitOnOverride(true)
    .build();

// Create a cache manager with the custom configuration
CacheManager cacheManager = new InMemoryCacheManager(config);

// Or create an individual cache with custom configuration
Cache<String, Document> documentCache = new InMemoryCache<>(config);
```

### Atomic Cache Modifications

```java
// Perform atomic modifications to a cache
InMemoryCacheManager cacheManager = new InMemoryCacheManager();
CacheIdentifier<String, User> userCacheId = 
    new CacheIdentifier<>(String.class, User.class, "userCache");
    
// This operation is atomic and thread-safe
cacheManager.modifyCache(userCacheId, cache -> {
    // Multiple operations on the cache
    cache.put("user1", new User("user1", "User One"));
    cache.put("user2", new User("user2", "User Two"));
    cache.invalidate("oldUser");
});
```

### Using Caches in a Dependency Injection Context

The cache-integration module provides a WireDI extension that allows you to inject caches into your classes.

```java
@Wire
public class UserService {
    private final Cache<String, User> userCache;
    
    @Inject
    public UserService(CacheManager cacheManager) {
        this.userCache = cacheManager.getCache(String.class, User.class, "userCache");
    }
    
    public User getUser(String username) {
        return userCache.getOr(username, () -> {
            // Fetch from database if not in cache
            return userRepository.findByUsername(username);
        });
    }
}
```

### Working with Different Eviction Policies

#### Least Recently Used (LRU)

```java
// Create a configuration with LRU eviction policy
InMemoryCacheConfiguration lruConfig = InMemoryCacheConfiguration.builder()
    .withCapacity(100)
    .withReorderOnHit(true)  // This enables LRU behavior
    .build();

Cache<String, Document> lruCache = new InMemoryCache<>(lruConfig);
```

#### Least Frequently Used (LFU)

The default behavior of InMemoryCache is to use a combination of hit counting and insertion order, which approximates an LFU policy.

```java
// Create a configuration that emphasizes hit counting (LFU-like)
InMemoryCacheConfiguration lfuConfig = InMemoryCacheConfiguration.builder()
    .withCapacity(100)
    .withReorderOnHit(false)  // Don't reorder on hit (use hit count only)
    .withHitOnOverride(true)  // Count overrides as hits
    .build();

Cache<String, Document> lfuCache = new InMemoryCache<>(lfuConfig);
```

## Configuration Options

### Available Configuration Parameters

The `InMemoryCacheConfiguration` record provides the following parameters:

- **hitOnOverride**: Determines if a cache hit should be counted when an existing entry is overridden
- **reorderOnHit**: Determines if cache entries should be reordered when accessed (implements LRU policy when true)
- **capacity**: The maximum number of entries the cache can hold

### Default Configuration

The default configuration has the following settings:
- hitOnOverride = true (overriding an entry counts as a hit)
- reorderOnHit = false (entries are not reordered when accessed)
- capacity = 50 (maximum of 50 entries in the cache)

```java
// Create a cache manager with the default configuration
CacheManager cacheManager = new InMemoryCacheManager(
    InMemoryCacheConfiguration.DEFAULT
);
```

### Creating Custom Configurations

```java
// Using the builder pattern
InMemoryCacheConfiguration config = InMemoryCacheConfiguration.builder()
    .withCapacity(100)
    .withHitOnOverride(true)
    .withReorderOnHit(true)
    .build();
```

## Best Practices

### Choosing Appropriate Cache Identifiers

- Use simple, immutable objects like Strings or Enums as cache identifiers
- Choose descriptive names that reflect the purpose of the cache
- Consider using a naming convention for cache identifiers

```java
// Using an enum for cache identifiers
enum CacheType { USERS, PRODUCTS, ORDERS }

Cache<String, User> userCache = cacheManager.getCache(
    String.class, User.class, CacheType.USERS
);
```

### Selecting the Right Eviction Policy

- Use LRU (reorderOnHit = true) when access patterns are more important than frequency
- Use LFU-like behavior (default) when frequency of access is more important
- Consider the nature of your data and access patterns when choosing a policy

### Managing Cache Capacity

- Set an appropriate capacity based on memory constraints and data size
- Monitor cache hit rates to optimize capacity
- Consider using different capacities for different caches based on their importance

### Thread Safety Considerations

- Use InMemoryCacheManager for thread-safe access to caches
- Use the modifyCache method for atomic operations on a cache
- Be aware that while InMemoryCacheManager provides thread-safe access, the Cache instances themselves are not thread-safe when accessed directly