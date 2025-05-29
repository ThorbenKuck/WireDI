# WireDI Development Guidelines

This document provides essential information for developers working on the WireDI project.

## Build/Configuration Instructions

### Prerequisites
- Java 21 or higher
- Maven 3.6 or higher

### Building the Project
The project uses Maven as its build system. To build the entire project:

```bash
mvn clean install
```

To build without running tests:

```bash
mvn clean install -DskipTests
```

### Project Structure
The project is organized into several main modules:
- **runtime**: Contains the runtime environment and libraries
- **compile-time**: Contains annotation processors and compile-time utilities
- **bundles**: Contains pre-configured bundles of WireDI components
- **integrations**: Contains integrations with other frameworks and libraries
- **test**: Contains integration tests

### Configuration
WireDI uses a hierarchical property system with the following precedence (highest to lowest):
1. System Properties
2. OS Environment Variables
3. Profile-specific properties (application-<profile>.properties)
4. Additional properties (specified by load.additional-properties)
5. Default properties (application.properties)

## Testing Information

### Testing Framework
WireDI uses JUnit 5 for testing with AssertJ for assertions and Mockito for mocking. JaCoCo is used for test coverage reporting.

### Running Tests
To run all tests:

```bash
mvn test
```

To run a specific test class:

```bash
mvn test -Dtest=FullyQualifiedClassName
```

### Writing Tests
Tests should follow these patterns:
1. Use the Arrange-Act-Assert pattern for test structure
2. Include descriptive failure messages in assertions
3. If a module provides an "AbstractIntegrationTest", use it for integration tests.
4. the _test folder contains a reference project for end-to-end integrations
   1. If possible, add an integrative example in this project

### Example Test
Here's a simple test that demonstrates how to test WireDI functionality:

```java
package com.wiredi.test;

import com.wiredi.runtime.WireRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class SimpleWireRepositoryTest extends AbstractIntegrationTest {

    @Test
    public void testGetExistingBean() {
        // Arrange
        WireRepository wireRepository = loadWireRepository();

        // Act
        CountInvocationsAspect aspect = wireRepository.get(CountInvocationsAspect.class);

        // Assert
        assertThat(aspect).isNotNull();
        assertThat(aspect.invocations()).isEqualTo(0);
    }
}
```

## Additional Development Information

### Code Style
- Use descriptive names for classes, methods, and variables
- Follow the Arrange-Act-Assert pattern in tests
- Include JavaDoc for all public classes and methods
- Use System.out.println("[DEBUG_LOG] ...") for debug logging in tests

### Dependency Injection
WireDI is a dependency injection framework that combines compile-time processing with runtime flexibility:
- Use `@Wire` to mark classes for dependency injection
- Use `@Inject` to mark constructor, field, or method injection points
  - Prefer constructor injection over method injection.
    - `@Inject` is optional as long a class has only one constructor.
  - Prefer method injection over field injection.
- Use `@PostConstruct` for initialization methods

### Aspects
WireDI supports aspect-oriented programming:
- Create an annotation and mark it with `@AspectTarget(aspect = YourAspect.class)`
- Create an aspect class with a method marked with `@Aspect(around = YourAnnotation.class)`
- The aspect method should take an `ExecutionContext` parameter and call `context.proceed()` to execute the original method

### Property Binding
WireDI supports binding properties to classes:
- Create a class with fields that match property names
- Use `@PropertyBinding` to bind properties to the class
- Use `@PropertyValue` to bind a specific property to a field

### Future Development
Planned features include:
- Support for nested classes in PropertyBinding
- Support for PropertyReference parameters in injection
- A "Strict" mode that does everything at compile time with limited runtime customization

### Development Workflow
1. Fork the repository and create a feature branch
2. Implement the feature or fix the bug
3. Add tests to verify the functionality
4. Ensure all tests pass and code quality checks succeed
5. Submit a pull request with a clear description of the changes

### Debugging
- Use `System.out.println("[DEBUG_LOG] ...")` for debug logging in tests
- Set the log level to DEBUG for more detailed logging
- Use the `WireRepository.diagnostics()` method to get information about the current state of the repository
