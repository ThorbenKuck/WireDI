# Wired Security

A standardized security API abstraction layer supporting multiple authentication methods and cryptographic operations.

## Core Features

- Thread-safe security context management
- Flexible authentication providers
- Industry-standard cryptographic algorithms

## Security Context

### Overview

The `SecurityContext` manages authentication state using thread-local storage. It processes authentication sources
through registered providers and maintains the current security state.

### Authentication Flow

1. `SecurityContext` receives an authentication source
2. Registered `AuthenticationProvider`s process the source
3. Valid authentication is stored in thread-local context
4. Secured operations access authentication through context
5. Context is cleared after operation completion

### Built-in Authentication Methods

- Username/Password authentication
- Token-based authentication
- Custom authentication providers

### Example: HTTP Authentication

Consider an HTTP server needing username/password authentication:

1. Create an authentication provider:

```java
public class HttpUsernamePasswordAuthenticationProvider implements AuthenticationProvider {
private final UserService userService;

    @Override
    public Authentication extract(Object source) {
        if (source instanceof HttpRequestMessageDetails details) {
            String username = details.request().parameters().get("username");
            String password = details.request().parameters().get("password");
            
            if (username != null && password != null) {
                return createAndValidateAuthentication(username, password);
            }
        }
        return null;
    }
}
```

2. Set up the security context:

```java
AuthenticationExtractor extractor = new AuthenticationExtractor(
        List.of(new HttpUsernamePasswordAuthenticationProvider(userService))
);
SecurityContext context = new SecurityContext(extractor);
```

## Cryptographic Module

### Supported Algorithms

| Algorithm | Features                                              | Use Case            |
|-----------|-------------------------------------------------------|---------------------|
| BCrypt    | - Configurable work factor- Automatic salt generation | Password hashing    |
| Argon2    | - Memory-hard algorithm- Configurable parameters      | Modern applications |

### Key Generation

```java
// Production: Random keys
KeyGenerator generator = KeyGenerator.random();

// Testing: Fixed keys
KeyGenerator testGenerator = KeyGenerator.just(knownKey);
```

#### Authentication

- Use security integration for automatic wiring
- Clear context after use
- Implement custom providers for specific needs

#### Cryptography

- Use modern algorithms
- Never store plain passwords
- Use secure random keys in production

#### Context Management

- Always clear context after use
- Use try-finally blocks
- Minimize sensitive data retention

## Testing Support

## Error Handling

### Common Exceptions

| Exception                      | Cause                     |
|--------------------------------|---------------------------|
| UnauthenticatedException       | No authentication present |
| InvalidAuthenticationException | Invalid credentials       |
| AlgorithmNotFoundException     | Missing crypto algorithm  |

---

> [!IMPORTANT]
> Always use the [security integration](../../../integrations/security) for automatic wiring in production environments.
