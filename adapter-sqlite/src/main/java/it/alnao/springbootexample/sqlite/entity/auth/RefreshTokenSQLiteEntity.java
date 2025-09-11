package it.alnao.springbootexample.sqlite.entity.auth;

import it.alnao.springbootexample.core.domain.auth.RefreshToken;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;

/**
 * Entity JPA per RefreshToken su SQLite
 */
@Entity
@Table(name = "refresh_tokens")
public class RefreshTokenSQLiteEntity {
    
    @Id
    @UuidGenerator
    private String id;
    
    @Column(nullable = false, unique = true, length = 500)
    private String token;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "last_used")
    private LocalDateTime lastUsed;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Costruttori
    public RefreshTokenSQLiteEntity() {}
    
    public RefreshTokenSQLiteEntity(RefreshToken refreshToken) {
        this.id = refreshToken.getId();
        this.token = refreshToken.getToken();
        this.userId = refreshToken.getUserId();
        this.expiryDate = refreshToken.getExpiryDate();
        this.createdAt = refreshToken.getCreatedAt();
        this.lastUsed = refreshToken.getLastUsed();
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
    
    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getLastUsed() { return lastUsed; }
    public void setLastUsed(LocalDateTime lastUsed) { this.lastUsed = lastUsed; }
}
