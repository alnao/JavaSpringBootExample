package it.alnao.springbootexample.core.domain.auth;

import java.time.LocalDateTime;

/**
 * Entità per i refresh token JWT.
 */
public class RefreshToken {
    private String id;
    private String token;
    private String userId;
    private LocalDateTime expiryDate;
    private LocalDateTime createdAt;
    private LocalDateTime lastUsed;

    // Constructors
    public RefreshToken() {
        this.createdAt = LocalDateTime.now();
    }

    public RefreshToken(String token, String userId, LocalDateTime expiryDate) {
        this();
        this.token = token;
        this.userId = userId;
        this.expiryDate = expiryDate;
    }

    // Helper methods
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
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
        return "RefreshToken{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", expiryDate=" + expiryDate +
                '}';
    }
}
