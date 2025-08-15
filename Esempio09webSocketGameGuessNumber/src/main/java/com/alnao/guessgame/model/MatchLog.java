package com.alnao.guessgame.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

/**
 * Match log entry for tracking game events
 */
@Document(collection = "match_log")
public class MatchLog {
    
    @Id
    private String id;
    private String event;
    private String connectionId;
    private String nickname;
    private LocalDateTime timestamp;
    private Object data; // Generic data field for event-specific information
    
    // Default constructor
    public MatchLog() {}
    
    // Constructor with basic fields
    public MatchLog(String event, String connectionId, String nickname) {
        this.event = event;
        this.connectionId = connectionId;
        this.nickname = nickname;
        this.timestamp = LocalDateTime.now();
    }
    
    // Constructor with data
    public MatchLog(String event, String connectionId, String nickname, Object data) {
        this(event, connectionId, nickname);
        this.data = data;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getEvent() {
        return event;
    }
    
    public void setEvent(String event) {
        this.event = event;
    }
    
    public String getConnectionId() {
        return connectionId;
    }
    
    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
}
