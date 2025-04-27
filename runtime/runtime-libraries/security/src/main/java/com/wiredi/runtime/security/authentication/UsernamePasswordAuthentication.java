package com.wiredi.runtime.security.authentication;

/**
 * A simple authentication implementation for username/password authentication.
 *
 * This class is a basic representation of a username/password authentication, so specific authentication must override
 * this class if additional information is needed.
 *
 * However, this class provides a convenient way to store the username and password in the authentication.
 * Additionally, you can translate user details and set the respective authorities instead of relying on the technology-specific implementation.
 *
 * @see Authentication
 * @see SimpleAuthentication
 * @see TokenAuthentication
 */
public class UsernamePasswordAuthentication extends SimpleAuthentication<UsernamePasswordAuthentication> {

    private String username;
    private String password;

    public UsernamePasswordAuthentication(
            String username,
            String password
    ) {
        this.username = username;
        this.password = password;
    }

    /**
     * Get the password of the user.
     *
     * @return the password of the user.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Get the username of the user.
     *
     * @return the username of the user.
     */
    public String getUsername() {
        return username;
    }
}
