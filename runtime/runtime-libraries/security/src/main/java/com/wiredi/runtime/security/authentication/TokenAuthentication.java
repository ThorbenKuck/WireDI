package com.wiredi.runtime.security.authentication;

import org.jetbrains.annotations.NotNull;

/**
 * This authentication represents any authentication that uses a token.
 * <p>
 * These can be, for example, JWT tokens or API keys.
 * A token can be used to authenticate a user, but doesn't necessarily have to be a username/password combination.
 * <p>
 * This class is a basic representation of a token authentication, so specific authentication must override this class if additional information is needed.
 * However, this class provides a convenient way to store the token in the authentication.
 * Additionally, you can translate the token and set the respective authorities instead of relying on the technology-specific implementation.
 *
 * @see Authentication
 * @see SimpleAuthentication
 * @see UsernamePasswordAuthentication
 */
public class TokenAuthentication extends SimpleAuthentication<UsernamePasswordAuthentication> {

    /**
     * The token used to authenticate.
     */
    @NotNull
    private final String token;

    public TokenAuthentication(@NotNull String token) {
        this.token = token;
    }

    /**
     * Returns the token used to authenticate.
     *
     * @return the token used to authenticate.
     */
    @NotNull
    public String getToken() {
        return token;
    }
}
