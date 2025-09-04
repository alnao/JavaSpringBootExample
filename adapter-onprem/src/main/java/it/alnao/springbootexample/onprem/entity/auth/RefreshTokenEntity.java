package it.alnao.springbootexample.onprem.entity.auth;

import it.alnao.springbootexample.port.domain.auth.RefreshToken;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entità JPA per PostgreSQL per i refresh token.
 */
@Entity
@Table(name = "refresh_tokens")
public class RefreshTokenEntity {
    
    @Id
    private String id;
    
    @Column(unique = true)
    private String token;
    
    @Column(name = "user_id")
    private String userId;
    
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "last_used")
    private LocalDateTime lastUsed;

    // Constructors
    public RefreshTokenEntity() {
        this.createdAt = LocalDateTime.now();
    }

    // Conversion methods
    public static RefreshTokenEntity fromDomain(RefreshToken refreshToken) {
        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setId(refreshToken.getId());
        entity.setToken(refreshToken.getToken());
        entity.setUserId(refreshToken.getUserId());
        entity.setExpiryDate(refreshToken.getExpiryDate());
        entity.setCreatedAt(refreshToken.getCreatedAt());
        entity.setLastUsed(refreshToken.getLastUsed());
        return entity;
    }

    public RefreshToken toDomain() {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(this.id);
        refreshToken.setToken(this.token);
        refreshToken.setUserId(this.userId);
        refreshToken.setExpiryDate(this.expiryDate);
        refreshToken.setCreatedAt(this.createdAt);
        refreshToken.setLastUsed(this.lastUsed);
        return refreshToken;
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
}
