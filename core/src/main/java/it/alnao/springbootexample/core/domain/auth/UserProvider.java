package it.alnao.springbootexample.core.domain.auth;

import java.time.LocalDateTime;

/**
 * Entità che rappresenta la connessione di un utente con un provider OAuth2.
 */
public class UserProvider {
    private String id;
    private String userId;
    private String provider; // GOOGLE, GITHUB, MICROSOFT, etc.
    private String providerUserId;
    private String providerEmail;
    private String providerUsername;
    private String accessTokenHash; // Hash del token per security
    private LocalDateTime createdAt;
    private LocalDateTime lastUsed;

    // Constructors
    public UserProvider() {
        this.createdAt = LocalDateTime.now();
        this.lastUsed = LocalDateTime.now();
    }

    public UserProvider(String userId, String provider, String providerUserId) {
        this();
        this.userId = userId;
        this.provider = provider;
        this.providerUserId = providerUserId;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getProviderUserId() {
        return providerUserId;
    }

    public void setProviderUserId(String providerUserId) {
        this.providerUserId = providerUserId;
    }

    public String getProviderEmail() {
        return providerEmail;
    }

    public void setProviderEmail(String providerEmail) {
        this.providerEmail = providerEmail;
    }

    public String getProviderUsername() {
        return providerUsername;
    }

    public void setProviderUsername(String providerUsername) {
        this.providerUsername = providerUsername;
    }

    public String getAccessTokenHash() {
        return accessTokenHash;
    }

    public void setAccessTokenHash(String accessTokenHash) {
        this.accessTokenHash = accessTokenHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(LocalDateTime lastUsed) {
        this.lastUsed = lastUsed;
    }

    @Override
    public String toString() {
        return "UserProvider{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", provider='" + provider + '\'' +
                ", providerUserId='" + providerUserId + '\'' +
                '}';
    }
}
