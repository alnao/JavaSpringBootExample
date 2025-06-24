package it.alnao.esempio04.repository;

import it.alnao.esempio04.model.User;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Repository
public class UserDynamoRepository {

    private final DynamoDbClient dynamoDbClient;

    //    private final String tableName = "users";
    @Value("${aws.dynamodb.tableName:null}")
    private String tableName;

    public UserDynamoRepository(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    public User save(User user) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", AttributeValue.builder().s(user.getId()).build());
        item.put("name", AttributeValue.builder().s(user.getName()).build());
        item.put("email", AttributeValue.builder().s(user.getEmail()).build());
        item.put("createdAt", AttributeValue.builder().s(user.getCreatedAt().toString()).build());
        item.put("updatedAt", AttributeValue.builder().s(user.getUpdatedAt().toString()).build());

        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();

        try {
            dynamoDbClient.putItem(putItemRequest);
            return user;
        } catch (DynamoDbException e) {
            throw new RuntimeException("Error saving user to DynamoDB: " + e.getMessage(), e);
        }
    }

    public Optional<User> findById(String id) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", AttributeValue.builder().s(id).build());

        GetItemRequest getItemRequest = GetItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .build();

        try {
            GetItemResponse response = dynamoDbClient.getItem(getItemRequest);
            
            if (response.hasItem()) {
                return Optional.of(mapToUser(response.item()));
            }
            return Optional.empty();
        } catch (DynamoDbException e) {
            throw new RuntimeException("Error getting user from DynamoDB: " + e.getMessage(), e);
        }
    }

    public List<User> findAll() {
        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(tableName)
                .build();

        try {
            ScanResponse response = dynamoDbClient.scan(scanRequest);
            List<User> users = new ArrayList<>();
            
            for (Map<String, AttributeValue> item : response.items()) {
                users.add(mapToUser(item));
            }
            
            return users;
        } catch (DynamoDbException e) {
            throw new RuntimeException("Error scanning users from DynamoDB: " + e.getMessage(), e);
        }
    }

    public void deleteById(String id) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", AttributeValue.builder().s(id).build());

        DeleteItemRequest deleteItemRequest = DeleteItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .build();

        try {
            dynamoDbClient.deleteItem(deleteItemRequest);
        } catch (DynamoDbException e) {
            throw new RuntimeException("Error deleting user from DynamoDB: " + e.getMessage(), e);
        }
    }

    private User mapToUser(Map<String, AttributeValue> item) {
        User user = new User();
        user.setId(getStringValue(item, "id"));
        user.setName(getStringValue(item, "name"));
        user.setEmail(getStringValue(item, "email"));
        
        String createdAtStr = getStringValue(item, "createdAt");
        if (createdAtStr != null) {
            user.setCreatedAt(Instant.parse(createdAtStr));
        }
        
        String updatedAtStr = getStringValue(item, "updatedAt");
        if (updatedAtStr != null) {
            user.setUpdatedAt(Instant.parse(updatedAtStr));
        }
        
        return user;
    }

    private String getStringValue(Map<String, AttributeValue> item, String key) {
        AttributeValue value = item.get(key);
        return value != null ? value.s() : null;
    }
}