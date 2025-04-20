package com.wiredi.integration.cache;

import com.wiredi.annotations.Provider;
import com.wiredi.annotations.stereotypes.AutoConfiguration;
import com.wiredi.runtime.ObjectReference;
import com.wiredi.runtime.domain.conditional.builtin.ConditionalOnMissingBean;
import com.wiredi.runtime.domain.conditional.builtin.ConditionalOnProperty;
import com.wiredi.runtime.security.SecurityContext;
import com.wiredi.runtime.security.authentication.AuthenticationExtractor;
import com.wiredi.runtime.security.authentication.AuthenticationProvider;
import com.wiredi.runtime.security.crypto.*;
import com.wiredi.runtime.security.crypto.generator.KeyGenerator;
import com.wiredi.runtime.security.crypto.generator.RandomKeyGenerator;
import jakarta.inject.Named;

import java.util.List;

@AutoConfiguration
@ConditionalOnMissingBean(type = SecurityContext.class)
@ConditionalOnProperty(
        key = "wiredi.security.autoconfigure",
        havingValue = "true",
        matchIfMissing = true
)
public class SecurityAutoConfiguration {

    @Provider
    public SecurityContext securityContext(AuthenticationExtractor extractor) {
        return new SecurityContext(extractor);
    }

    @Provider
    public AuthenticationExtractor authenticationExtractor(List<AuthenticationProvider> authenticationProvider) {
        return new AuthenticationExtractor(authenticationProvider);
    }

    @Provider
    public Algorithms algorithms(List<CryptographicAlgorithm> algorithms, ObjectReference<CryptographicAlgorithm> systemAlgorithm) {
        return new Algorithms(algorithms, systemAlgorithm.getInstance(NoOpAlgorithm::new));
    }

    @Provider
    @ConditionalOnMissingBean(type = KeyGenerator.class)
    public KeyGenerator keyGenerator() {
        return new RandomKeyGenerator();
    }

    @Provider
    @Named("argon2")
    @ConditionalOnMissingBean(type = Argon2Algorithm.class)
    public Argon2Algorithm argon2Algorithm(KeyGenerator keyGenerator) {
        return new Argon2Algorithm(keyGenerator);
    }

    @Provider
    @Named("bcrypt")
    @ConditionalOnMissingBean(type = BCryptAlgorithm.class)
    public BCryptAlgorithm bcryptAlgorithm(KeyGenerator keyGenerator) {
        return new BCryptAlgorithm(keyGenerator);
    }
}
