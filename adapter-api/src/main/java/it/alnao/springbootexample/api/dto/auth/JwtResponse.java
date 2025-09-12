package it.alnao.springbootexample.api.dto.auth;

import it.alnao.springbootexample.core.domain.auth.AccountType;

import java.util.List;

/**
 * DTO per la risposta JWT.
 */
public class JwtResponse {
    private String token;
    private String username;
    private String email;
    private AccountType accountType;
    private String role;
    private List<String> linkedProviders;
    private String tokenType = "Bearer";
    private long expiresIn;

    // Constructors
    public JwtResponse() {}

    public JwtResponse(String token, String username, AccountType accountType) {
        this.token = token;
        this.username = username;
        this.accountType = accountType;
    }

    public JwtResponse(String token, String username, String email, AccountType accountType, String role, long expiresIn) {
        this.token = token;
        this.username = username;
        this.email = email;
        this.accountType = accountType;
        this.role = role;
        this.expiresIn = expiresIn;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<String> getLinkedProviders() {
        return linkedProviders;
    }

    public void setLinkedProviders(List<String> linkedProviders) {
        this.linkedProviders = linkedProviders;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    @Override
    public String toString() {
        return "JwtResponse{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", accountType=" + accountType +
                ", role='" + role + '\'' +
                ", tokenType='" + tokenType + '\'' +
                ", expiresIn=" + expiresIn +
                '}';
    }
}
