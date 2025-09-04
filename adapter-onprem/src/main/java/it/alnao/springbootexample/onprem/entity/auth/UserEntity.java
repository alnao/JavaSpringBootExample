package it.alnao.springbootexample.onprem.entity.auth;

import it.alnao.springbootexample.port.domain.auth.User;
import it.alnao.springbootexample.port.domain.auth.UserRole;
import it.alnao.springbootexample.port.domain.auth.AccountType;
import it.alnao.springbootexample.port.domain.auth.UserProvider;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entità JPA per PostgreSQL per la gestione degli utenti.
 */
@Entity
@Table(name = "users")
public class UserEntity {
    
    @Id
    private String id;
    
    @Column(unique = true)
    private String username;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    private String password;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    @Column(name = "avatar_url")
    private String avatarUrl;
    
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.USER;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type")
    private AccountType accountType = AccountType.LOCAL;
    
    @Column(name = "external_id")
    private String externalId;
    private boolean enabled = true;
    @Column(name = "email_verified")
    private boolean emailVerified = false;
    
    @OneToMany(mappedBy = "userId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserProviderEntity> providers = new ArrayList<>();
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    // Constructors
    public UserEntity() {
        this.createdAt = LocalDateTime.now();
    }

    // Conversion methods
    public static UserEntity fromDomain(User user) {
        UserEntity entity = new UserEntity();
        entity.setId(user.getId());
        entity.setUsername(user.getUsername());
        entity.setEmail(user.getEmail());
        entity.setPassword(user.getPassword());
        entity.setFirstName(user.getFirstName());
        entity.setLastName(user.getLastName());
        entity.setAvatarUrl(user.getAvatarUrl());
        entity.setRole(user.getRole());
        entity.setAccountType(user.getAccountType());
        entity.setExternalId(user.getExternalId());
        entity.setEnabled(user.isEnabled());
        entity.setEmailVerified(user.isEmailVerified());
        entity.setCreatedAt(user.getCreatedAt());
        entity.setLastLogin(user.getLastLogin());
        return entity;
    }

    public User toDomain() {
        User user = new User();
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
        user.setLastLogin(this.lastLogin);
        
        // Convert providers
        List<UserProvider> domainProviders = new ArrayList<>();
        for (UserProviderEntity providerEntity : this.providers) {
            domainProviders.add(providerEntity.toDomain());
        }
        user.setProviders(domainProviders);
        
        return user;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public List<UserProviderEntity> getProviders() {
        return providers;
    }

    public void setProviders(List<UserProviderEntity> providers) {
        this.providers = providers;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }
}
