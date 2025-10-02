package it.alnao.springbootexample.azure.entity.auth;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_provider")
public class UserProviderSqlServerEntity {
    public String getId() {
        return id;
    }
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
    // Getters e setters ...

    public String getUserId() {
        return userId;
    }

    public String getProvider() {
        return provider;
    }

    public static UserProviderSqlServerEntity fromDomain(it.alnao.springbootexample.core.domain.auth.UserProvider userProvider) {
        UserProviderSqlServerEntity entity = new UserProviderSqlServerEntity();
        entity.id = userProvider.getId();
        entity.userId = userProvider.getUserId();
        entity.provider = userProvider.getProvider();
        entity.providerUserId = userProvider.getProviderUserId();
        entity.providerEmail = userProvider.getProviderEmail();
        entity.providerUsername = userProvider.getProviderUsername();
        entity.accessTokenHash = userProvider.getAccessTokenHash();
        entity.createdAt = userProvider.getCreatedAt();
        entity.lastUsed = userProvider.getLastUsed();
        return entity;
    }

    public it.alnao.springbootexample.core.domain.auth.UserProvider toDomain() {
        it.alnao.springbootexample.core.domain.auth.UserProvider userProvider = new it.alnao.springbootexample.core.domain.auth.UserProvider();
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
}
