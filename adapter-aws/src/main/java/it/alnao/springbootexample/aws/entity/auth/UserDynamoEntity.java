package it.alnao.springbootexample.aws.entity.auth;

import it.alnao.springbootexample.port.domain.auth.AccountType;
import it.alnao.springbootexample.port.domain.auth.User;
import it.alnao.springbootexample.port.domain.auth.UserRole;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Entità DynamoDB per gli utenti.
 */
@DynamoDbBean
public class UserDynamoEntity {
    
    private String id;
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String avatarUrl;
    private String role;
    private String accountType;
    private String externalId;
    private Boolean enabled;
    private Boolean emailVerified;
    private String createdAt;
    private String updatedAt;
    private String lastLogin;

    // Constructors
    public UserDynamoEntity() {
        this.enabled = true;
        this.emailVerified = false;
        this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    // Conversion methods
    public static UserDynamoEntity fromDomain(User user) {
        UserDynamoEntity entity = new UserDynamoEntity();
        entity.setId(user.getId());
        entity.setUsername(user.getUsername());
        entity.setEmail(user.getEmail());
        entity.setPassword(user.getPassword());
        entity.setFirstName(user.getFirstName());
        entity.setLastName(user.getLastName());
        entity.setAvatarUrl(user.getAvatarUrl());
        entity.setRole(user.getRole().getRoleName());
        entity.setAccountType(user.getAccountType().name());
        entity.setExternalId(user.getExternalId());
        entity.setEnabled(user.isEnabled());
        entity.setEmailVerified(user.isEmailVerified());
        
        if (user.getCreatedAt() != null) {
            entity.setCreatedAt(user.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        if (user.getUpdatedAt() != null) {
            entity.setUpdatedAt(user.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        if (user.getLastLogin() != null) {
            entity.setLastLogin(user.getLastLogin().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        
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
        user.setRole(UserRole.valueOf(this.role));
        user.setAccountType(AccountType.valueOf(this.accountType));
        user.setExternalId(this.externalId);
        user.setEnabled(this.enabled != null ? this.enabled : true);
        user.setEmailVerified(this.emailVerified != null ? this.emailVerified : false);
        
        if (this.createdAt != null) {
            user.setCreatedAt(LocalDateTime.parse(this.createdAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        if (this.updatedAt != null) {
            user.setUpdatedAt(LocalDateTime.parse(this.updatedAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        if (this.lastLogin != null) {
            user.setLastLogin(LocalDateTime.parse(this.lastLogin, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        
        return user;
    }

    // DynamoDB annotations and getters/setters
    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "username-index")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "email-index")
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "external-id-index")
    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(String lastLogin) {
        this.lastLogin = lastLogin;
    }
}
