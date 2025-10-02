package it.alnao.springbootexample.azure.entity.auth;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_token")
public class RefreshTokenSqlServerEntity {
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
    // Getters e setters ...

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public static RefreshTokenSqlServerEntity fromDomain(it.alnao.springbootexample.core.domain.auth.RefreshToken refreshToken) {
        RefreshTokenSqlServerEntity entity = new RefreshTokenSqlServerEntity();
        entity.id = refreshToken.getId();
        entity.token = refreshToken.getToken();
        entity.userId = refreshToken.getUserId();
        entity.expiryDate = refreshToken.getExpiryDate();
        entity.createdAt = refreshToken.getCreatedAt();
        entity.lastUsed = refreshToken.getLastUsed();
        return entity;
    }

    public it.alnao.springbootexample.core.domain.auth.RefreshToken toDomain() {
        it.alnao.springbootexample.core.domain.auth.RefreshToken refreshToken = new it.alnao.springbootexample.core.domain.auth.RefreshToken();
        refreshToken.setId(this.id);
        refreshToken.setToken(this.token);
        refreshToken.setUserId(this.userId);
        refreshToken.setExpiryDate(this.expiryDate);
        refreshToken.setCreatedAt(this.createdAt);
        refreshToken.setLastUsed(this.lastUsed);
        return refreshToken;
    }
}
