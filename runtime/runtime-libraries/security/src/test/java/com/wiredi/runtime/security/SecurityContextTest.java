package com.wiredi.runtime.security;

import com.wiredi.runtime.security.authentication.AuthenticationExtractor;
import com.wiredi.runtime.security.exceptions.UnauthenticatedException;
import com.wiredi.runtime.security.infrastructure.ConsumerRecord;
import com.wiredi.runtime.security.infrastructure.ServletRequest;
import com.wiredi.runtime.security.infrastructure.ServletRequestAuthenticationProvider;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SecurityContextTest {

    @Test
    public void securityContextSetsUpTheThreadLocalAuthentication() {
        // Arrange
        SecurityContext securityContext = new SecurityContext(
                new AuthenticationExtractor(
                        List.of(new ServletRequestAuthenticationProvider())
                )
        );
        ServletRequest request = new ServletRequest();
        request.setHeader("username", "user");
        request.setHeader("password", "pass");

        // Act
        securityContext.runAuthenticated(request, authentication -> {
            // Assert
            assertSame(authentication, securityContext.getAuthentication());
            assertSame(authentication, SecurityContext.current().getAuthentication());
            assertSame(authentication, SecurityContext.currentAuthentication());

            assertTrue(authentication.isChecked());
            assertTrue(authentication.isAuthenticated());
        });

        assertNull(securityContext.getAuthentication());
        assertNull(SecurityContext.current());
        assertNull(SecurityContext.currentAuthentication());
    }

    @Test
    public void ifTheAuthenticationCannotBeExtractedAnExceptionIsRaised() {
        // Arrange
        SecurityContext securityContext = new SecurityContext(
                new AuthenticationExtractor(
                        List.of(new ServletRequestAuthenticationProvider())
                )
        );
        ConsumerRecord request = new ConsumerRecord();
        request.setHeader("username", "user");
        request.setHeader("password", "pass");

        // Act Assert
        assertThrows(UnauthenticatedException.class, () -> securityContext.runAuthenticated(request, () -> {
        }));
    }

    @Test
    public void ifTheAuthenticationCanBeExtractedButIsWrongItStillExecutesTheCode() {
        // Arrange
        SecurityContext securityContext = new SecurityContext(
                new AuthenticationExtractor(
                        List.of(new ServletRequestAuthenticationProvider())
                )
        );
        ServletRequest request = new ServletRequest();
        request.setHeader("username", "not-user");
        request.setHeader("password", "not-pass");

        // Act
        securityContext.runAuthenticated(request, authentication -> {
            // Assert
            assertSame(authentication, securityContext.getAuthentication());
            assertSame(authentication, SecurityContext.current().getAuthentication());
            assertSame(authentication, SecurityContext.currentAuthentication());

            assertTrue(authentication.isChecked());
            assertFalse(authentication.isAuthenticated());
        });

        assertNull(securityContext.getAuthentication());
        assertNull(SecurityContext.current());
        assertNull(SecurityContext.currentAuthentication());
    }
}