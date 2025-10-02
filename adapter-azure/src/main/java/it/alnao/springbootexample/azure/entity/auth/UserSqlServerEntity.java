package it.alnao.springbootexample.azure.entity.auth;

import it.alnao.springbootexample.core.domain.auth.AccountType;
import it.alnao.springbootexample.core.domain.auth.UserRole;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user")
public class UserSqlServerEntity {
    @Id
    private String id;
    @Column(unique = true, length = 100)
    private String username;
    @Column(nullable = false, unique = true)
    private String email;
    private String password;
    @Column(name = "first_name", length = 100)
    private String firstName;
    @Column(name = "last_name", length = 100)
    private String lastName;
    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.USER;
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType = AccountType.LOCAL;
    @Column(name = "external_id")
    private String externalId;
    private Boolean enabled = true;
    @Column(name = "email_verified")
    private Boolean emailVerified = false;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    // Getters e setters ...

    public static UserSqlServerEntity fromDomain(it.alnao.springbootexample.core.domain.auth.User user) {
        UserSqlServerEntity entity = new UserSqlServerEntity();
        entity.id = user.getId();
        entity.username = user.getUsername();
        entity.email = user.getEmail();
        entity.password = user.getPassword();
        entity.firstName = user.getFirstName();
        entity.lastName = user.getLastName();
        entity.avatarUrl = user.getAvatarUrl();
        entity.role = user.getRole();
        entity.accountType = user.getAccountType();
        entity.externalId = user.getExternalId();
        entity.enabled = user.isEnabled();
        entity.emailVerified = user.isEmailVerified();
        entity.createdAt = user.getCreatedAt();
        entity.updatedAt = user.getUpdatedAt();
        entity.lastLogin = user.getLastLogin();
        return entity;
    }

    public it.alnao.springbootexample.core.domain.auth.User toDomain() {
        it.alnao.springbootexample.core.domain.auth.User user = new it.alnao.springbootexample.core.domain.auth.User();
        user.setId(this.id);
        user.setUsername(this.username);
        user.setEmail(this.email);
        user.setPassword(this.password);
        user.setFirstName(this.firstName);
        user.setLastName(this.lastName);
        user.setAvatarUrl(this.avatarUrl);
        user.setRole(this.role);
        user.setAccountType(this.accountType);
        user.setExternalId(this.externalId);
        user.setEnabled(this.enabled);
        user.setEmailVerified(this.emailVerified);
        user.setCreatedAt(this.createdAt);
        user.setUpdatedAt(this.updatedAt);
        user.setLastLogin(this.lastLogin);
        return user;
    }
}
