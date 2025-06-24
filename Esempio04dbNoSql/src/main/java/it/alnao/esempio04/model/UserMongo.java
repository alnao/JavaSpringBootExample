package it.alnao.esempio04.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

@Document(collection = "users")
public class UserMongo {
    
    @Id
    private String id;

    @NotBlank(message = "Name is mandatory")
    private String name;
    private String email;
    private Instant createdAt;
    private Instant updatedAt;

    public UserMongo() {}

    public UserMongo(String name, String email) {
        this.name = name;
        this.email = email;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { 
        this.name = name; 
        this.updatedAt = Instant.now();
    }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { 
        this.email = email; 
        this.updatedAt = Instant.now();
    }
    
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}