package it.alnao.springbootexample.port.domain.auth;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entità User per il sistema di autenticazione ibrido.
 * Supporta sia utenti locali che OAuth2.
 */
public class User {
    private String id;
    private String username;
    private String email;
    private String password; // NULL per utenti OAuth2
    private String firstName;
    private String lastName;
    private String avatarUrl;
    private UserRole role = UserRole.USER;
    private AccountType accountType = AccountType.LOCAL;
    private String externalId; // ID nel provider OAuth2
    private boolean enabled = true;
    private boolean emailVerified = false;
    private List<UserProvider> providers = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLogin;

    // Constructors
    public User() {
        this.createdAt = LocalDateTime.now();
    }

    public User(String username, String email, String password) {
        this();
        this.username = username;
        this.email = email;
        this.password = password;
        this.accountType = AccountType.LOCAL;
    }

    public User(String email, String firstName, String lastName, AccountType accountType, String externalId) {
        this();
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.accountType = accountType;
        this.externalId = externalId;
        this.emailVerified = true; // OAuth2 users have verified emails
    }

    // Helper methods
    public boolean isLocalAccount() {
        return accountType == AccountType.LOCAL;
    }

    public boolean isOAuth2Account() {
        return accountType != AccountType.LOCAL;
    }

    public boolean hasProvider(String provider) {
        return providers.stream()
                .anyMatch(p -> p.getProvider().equalsIgnoreCase(provider));
    }

    public String getDisplayName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        }
        if (firstName != null) {
            return firstName;
        }
        if (username != null) {
            return username;
        }
        return email;
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

    public List<UserProvider> getProviders() {
        return providers;
    }

    public void setProviders(List<UserProvider> providers) {
        this.providers = providers;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", accountType=" + accountType +
                ", role=" + role +
                ", enabled=" + enabled +
                '}';
    }
}
