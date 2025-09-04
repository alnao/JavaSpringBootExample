package it.alnao.springbootexample.aws.entity.auth;

import it.alnao.springbootexample.port.domain.auth.RefreshToken;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Entità DynamoDB per i refresh token.
 */
@DynamoDbBean
public class RefreshTokenDynamoEntity {
    
    private String id;
    private String token;
    private String userId;
    private String expiryDate;
    private String createdAt;
    private String lastUsed;

    // Constructors
    public RefreshTokenDynamoEntity() {
        this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    // Conversion methods
    public static RefreshTokenDynamoEntity fromDomain(RefreshToken refreshToken) {
        RefreshTokenDynamoEntity entity = new RefreshTokenDynamoEntity();
        entity.setId(refreshToken.getId());
        entity.setToken(refreshToken.getToken());
        entity.setUserId(refreshToken.getUserId());
        
        if (refreshToken.getExpiryDate() != null) {
            entity.setExpiryDate(refreshToken.getExpiryDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        if (refreshToken.getCreatedAt() != null) {
            entity.setCreatedAt(refreshToken.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        if (refreshToken.getLastUsed() != null) {
            entity.setLastUsed(refreshToken.getLastUsed().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        
        return entity;
    }

    public RefreshToken toDomain() {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(this.id);
        refreshToken.setToken(this.token);
        refreshToken.setUserId(this.userId);
        
        if (this.expiryDate != null) {
            refreshToken.setExpiryDate(LocalDateTime.parse(this.expiryDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        if (this.createdAt != null) {
            refreshToken.setCreatedAt(LocalDateTime.parse(this.createdAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        if (this.lastUsed != null) {
            refreshToken.setLastUsed(LocalDateTime.parse(this.lastUsed, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        
        return refreshToken;
    }

    // DynamoDB annotations and getters/setters
    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "token-index")
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "user-id-index")
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(String lastUsed) {
        this.lastUsed = lastUsed;
    }
}
