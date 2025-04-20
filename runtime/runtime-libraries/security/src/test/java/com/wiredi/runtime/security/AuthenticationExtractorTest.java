package com.wiredi.runtime.security;

import com.wiredi.runtime.security.authentication.Authentication;
import com.wiredi.runtime.security.authentication.AuthenticationExtractor;
import com.wiredi.runtime.security.authentication.UsernamePasswordAuthentication;
import com.wiredi.runtime.security.infrastructure.ConsumerRecordAuthenticationProvider;
import com.wiredi.runtime.security.infrastructure.ServletRequest;
import com.wiredi.runtime.security.infrastructure.ServletRequestAuthenticationProvider;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class AuthenticationExtractorTest {

    @Test
    public void matchingSourcesWithCorrectCredentialsWork() {
        // Arrange
        AuthenticationExtractor context = new AuthenticationExtractor(
                List.of(new ServletRequestAuthenticationProvider(), new ConsumerRecordAuthenticationProvider())
        );
        ServletRequest request = new ServletRequest();
        request.setHeader("username", "user");
        request.setHeader("password", "pass");

        // Act
        Authentication authentication = context.getAuthentication(request);

        // Assert
        assertThat(authentication)
                .isNotNull()
                .isInstanceOf(UsernamePasswordAuthentication.class)
                .matches(auth -> ((UsernamePasswordAuthentication) auth).getUsername().equals("user"))
                .matches(auth -> ((UsernamePasswordAuthentication) auth).getPassword().equals("pass"))
                .matches(auth -> auth.authorities().isEmpty())
                .matches(auth -> auth.state() == Authentication.State.AUTHENTICATED)
                .matches(Authentication::isAuthenticated)
                .matches(Authentication::isChecked);
    }

    @Test
    public void matchingSourcesWithInCorrectCredentialsExtractTheAuthenticationButIsNotAuthenticated() {
        // Arrange
        AuthenticationExtractor context = new AuthenticationExtractor(
                List.of(new ServletRequestAuthenticationProvider(), new ConsumerRecordAuthenticationProvider())
        );
        ServletRequest request = new ServletRequest();
        request.setHeader("username", "not-user");
        request.setHeader("password", "not-pass");

        // Act
        Authentication authentication = context.getAuthentication(request);

        // Assert
        assertThat(authentication)
                .isNotNull()
                .isInstanceOf(UsernamePasswordAuthentication.class)
                .matches(auth -> ((UsernamePasswordAuthentication) auth).getUsername().equals("not-user"))
                .matches(auth -> ((UsernamePasswordAuthentication) auth).getPassword().equals("not-pass"))
                .matches(auth -> auth.authorities().isEmpty())
                .matches(auth -> auth.state() == Authentication.State.NOT_AUTHENTICATED)
                .matches(auth -> !auth.isAuthenticated())
                .matches(Authentication::isChecked);
    }

    @Test
    public void missingProvidersReturnNullAsAuthentication() {
        // Arrange
        AuthenticationExtractor context = new AuthenticationExtractor(
                List.of(new ConsumerRecordAuthenticationProvider())
        );
        ServletRequest request = new ServletRequest();
        request.setHeader("username", "user");
        request.setHeader("password", "pass");

        // Act
        Authentication authentication = context.getAuthentication(request);

        // Assert
        assertThat(authentication).isNull();
    }
}
