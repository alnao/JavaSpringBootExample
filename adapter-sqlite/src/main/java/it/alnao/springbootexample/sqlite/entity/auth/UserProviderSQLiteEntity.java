package it.alnao.springbootexample.sqlite.entity.auth;

import it.alnao.springbootexample.port.domain.auth.UserProvider;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;

/**
 * Entity JPA per UserProvider su SQLite .
 */
@Entity
@Table(name = "user_providers")
public class UserProviderSQLiteEntity {
    public static UserProviderSQLiteEntity fromDomain(UserProvider userProvider) {
        return new UserProviderSQLiteEntity(userProvider);
    }
    
    @Id
    @UuidGenerator
    private String id;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(nullable = false, length = 50)
    private String provider;
    
    @Column(name = "external_id", nullable = false)
    private String externalId;
    
    @Column(name = "provider_email")
    private String providerEmail;
    
    @Column(name = "provider_username")
    private String providerUsername;
    
    @Column(name = "access_token_hash", length = 500)
    private String accessTokenHash;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "last_used")
    private LocalDateTime lastUsed;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Costruttori
    public UserProviderSQLiteEntity() {}
    
    public UserProviderSQLiteEntity(UserProvider userProvider) {
    this.id = userProvider.getId();
        this.userId = userProvider.getUserId();
        this.provider = userProvider.getProvider();
    this.externalId = userProvider.getProviderUserId();
        this.providerEmail = userProvider.getProviderEmail();
        this.providerUsername = userProvider.getProviderUsername();
        this.accessTokenHash = userProvider.getAccessTokenHash();
        this.createdAt = userProvider.getCreatedAt();
        this.lastUsed = userProvider.getLastUsed();
    }
    
    public UserProvider toDomain() {
        UserProvider userProvider = new UserProvider();
    userProvider.setId(this.id);
        userProvider.setUserId(this.userId);
        userProvider.setProvider(this.provider);
    userProvider.setProviderUserId(this.externalId);
        userProvider.setProviderEmail(this.providerEmail);
        userProvider.setProviderUsername(this.providerUsername);
        userProvider.setAccessTokenHash(this.accessTokenHash);
        userProvider.setCreatedAt(this.createdAt);
        userProvider.setLastUsed(this.lastUsed);
        return userProvider;
    }
    
    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    
    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
    
    public String getProviderEmail() { return providerEmail; }
    public void setProviderEmail(String providerEmail) { this.providerEmail = providerEmail; }
    
    public String getProviderUsername() { return providerUsername; }
    public void setProviderUsername(String providerUsername) { this.providerUsername = providerUsername; }
    
    public String getAccessTokenHash() { return accessTokenHash; }
    public void setAccessTokenHash(String accessTokenHash) { this.accessTokenHash = accessTokenHash; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getLastUsed() { return lastUsed; }
    public void setLastUsed(LocalDateTime lastUsed) { this.lastUsed = lastUsed; }
}
