package it.alnao.springbootexample.aws.entity.auth;

import it.alnao.springbootexample.port.domain.auth.UserProvider;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entità JPA per MySQL per i provider OAuth2.
 */
@Entity // nota: hibernate crea la tabella se non esiste , ma packages-to-scan deve includere questo package
@Table(name = "nome_tabella_user_provider") //see CustomPhysicalNamingStrategy
public class UserProviderMySQLEntity {
    @Id
    private String id;

    @Column(name = "user_id")
    private String userId;

    private String provider;

    @Column(name = "external_id")
    private String providerUserId;

    @Column(name = "provider_email")
    private String providerEmail;

    @Column(name = "provider_username")
    private String providerUsername;

    @Column(name = "access_token_hash")
    private String accessTokenHash;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_used")
    private LocalDateTime lastUsed;

    public UserProviderMySQLEntity() {
        this.createdAt = LocalDateTime.now();
        this.lastUsed = LocalDateTime.now();
    }

    public static UserProviderMySQLEntity fromDomain(UserProvider userProvider) {
        UserProviderMySQLEntity entity = new UserProviderMySQLEntity();
        entity.setId(userProvider.getId());
        entity.setUserId(userProvider.getUserId());
        entity.setProvider(userProvider.getProvider());
        entity.setProviderUserId(userProvider.getProviderUserId());
        entity.setProviderEmail(userProvider.getProviderEmail());
        entity.setProviderUsername(userProvider.getProviderUsername());
        entity.setAccessTokenHash(userProvider.getAccessTokenHash());
        entity.setCreatedAt(userProvider.getCreatedAt());
        entity.setLastUsed(userProvider.getLastUsed());
        return entity;
    }

    public UserProvider toDomain() {
        UserProvider userProvider = new UserProvider();
        userProvider.setId(this.id);
        userProvider.setUserId(this.userId);
        userProvider.setProvider(this.provider);
        userProvider.setProviderUserId(this.providerUserId);
        userProvider.setProviderEmail(this.providerEmail);
        userProvider.setProviderUsername(this.providerUsername);
        userProvider.setAccessTokenHash(this.accessTokenHash);
        userProvider.setCreatedAt(this.createdAt);
        userProvider.setLastUsed(this.lastUsed);
        return userProvider;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getProviderUserId() { return providerUserId; }
    public void setProviderUserId(String providerUserId) { this.providerUserId = providerUserId; }
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
