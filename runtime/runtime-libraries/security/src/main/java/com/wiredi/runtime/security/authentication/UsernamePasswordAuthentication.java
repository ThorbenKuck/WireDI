package com.wiredi.runtime.security.authentication;

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

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }
}
