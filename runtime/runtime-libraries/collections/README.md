# WireDI Collections Library

Practical collection helpers for Java with a strong focus on usability: type-keyed maps that are safe across classloader reloads, fast enum lookups, and lightweight paging primitives you can use in repositories and services.

## What’s inside

- TypeMap and ConcurrentTypeMap: Map values by their Java type without holding strong references to Class objects.
- EnumSet: O(1) lookups from names to enum constants with convenient case-insensitive matching.
- Paging API: Page, Pageable, Paged, Unpaged, Sort, and Order for describing and transporting paged results.

## Quick start

### TypeMap
Store and retrieve instances keyed by their type. Internally keys are the fully qualified class names, making it resilient to dynamic classloader changes.

```java
class Registry {
  private final TypeMap<Object> byType = new TypeMap<>();

  public <T> void register(Class<T> type, T instance) {
    byType.put(type, instance);
  }

  public <T> T get(Class<T> type) {
    return type.cast(byType.get(type));
  }
}

Registry r = new Registry();
r.register(Number.class, 42);
Number n = r.get(Number.class);            // 42
Integer i = r.get(Integer.class);          // null
```

Use ConcurrentTypeMap for thread-safe concurrent access:

```java
ConcurrentTypeMap<Object> cache = new ConcurrentTypeMap<>();
Object obj = cache.computeIfAbsent(String.class, () -> expensiveLoad());
```

### EnumSet
Look up enum constants by name in O(1) time, with a convenient case-insensitive variant that accepts kebab-case.

```java
enum Status { NEW_ORDER, IN_PROGRESS, DONE }

EnumSet<Status> set = EnumSet.of(Status.class);
set.get("IN_PROGRESS").ifPresent(System.out::println);      // IN_PROGRESS
set.getIgnoreCase("in-progress").ifPresent(System.out::println); // IN_PROGRESS
Status done = set.require("DONE"); // throws if missing
```

### Paging
Represent pages of results with metadata and sorting.

```java
// Describe the request
Pageable pageable = new Paged(0, 20, Sort.by("lastName", "firstName"));

// Build a page from existing data
List<String> data = List.of("Alice", "Bob", "Carol");
Page<String> page = new ListPage<>(1, data.size(), data, pageable);

// Transform elements while keeping metadata
Page<Integer> lengths = page.map(String::length);
```

Sort and Order can be combined:

```java
Sort sort = Sort.by("lastName").and(Sort.by(Sort.Direction.DESC, "createdAt"));
Pageable unpaged = Pageable.unpaged(sort);
```

## Design notes

- Type safety: Prefer Class keys to stringly-typed maps. Internally we still use class names to avoid pinning classes in memory across classloader reloads.
- Thread safety: TypeMap is not thread-safe; use ConcurrentTypeMap for concurrent access.
- Performance: EnumSet precomputes an immutable map for constant-time name lookups.
- Simplicity: Paging types are small value objects suitable for APIs and tests.

## Related packages

- Package: `com.wiredi.runtime.collections`
  - TypeMap, ConcurrentTypeMap, EnumSet
- Package: `com.wiredi.runtime.collections.pages`
  - Page, Pageable, Paged, Unpaged, Sort, Order, ListPage, AbstractPage
