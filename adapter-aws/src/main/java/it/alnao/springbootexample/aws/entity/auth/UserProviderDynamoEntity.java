package it.alnao.springbootexample.aws.entity.auth;

import it.alnao.springbootexample.port.domain.auth.UserProvider;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Entità DynamoDB per i provider OAuth2 degli utenti.
 */
@DynamoDbBean
public class UserProviderDynamoEntity {
    
    private String id;
    private String userId;
    private String provider;
    private String providerUserId;
    private String providerEmail;
    private String createdAt;

    // Constructors
    public UserProviderDynamoEntity() {
        this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    // Conversion methods
    public static UserProviderDynamoEntity fromDomain(UserProvider userProvider) {
        UserProviderDynamoEntity entity = new UserProviderDynamoEntity();
        entity.setId(userProvider.getId());
        entity.setUserId(userProvider.getUserId());
        entity.setProvider(userProvider.getProvider());
        entity.setProviderUserId(userProvider.getProviderUserId());
        entity.setProviderEmail(userProvider.getProviderEmail());
        
        if (userProvider.getCreatedAt() != null) {
            entity.setCreatedAt(userProvider.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        
        return entity;
    }

    public UserProvider toDomain() {
        UserProvider userProvider = new UserProvider();
        userProvider.setId(this.id);
        userProvider.setUserId(this.userId);
        userProvider.setProvider(this.provider);
        userProvider.setProviderUserId(this.providerUserId);
        userProvider.setProviderEmail(this.providerEmail);
        
        if (this.createdAt != null) {
            userProvider.setCreatedAt(LocalDateTime.parse(this.createdAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        
        return userProvider;
    }

    // DynamoDB annotations and getters/setters
    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "user-id-index")
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "provider-external-index")
    public String getProviderUserId() {
        return providerUserId;
    }

    public void setProviderUserId(String providerUserId) {
        this.providerUserId = providerUserId;
    }

    public String getProviderEmail() {
        return providerEmail;
    }

    public void setProviderEmail(String providerEmail) {
        this.providerEmail = providerEmail;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
