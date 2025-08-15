package com.alnao.guessgame.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

/**
 * Player entity representing a player in the game
 */
@Document(collection = "players")
public class Player {
    
    @Id
    private String connectionId;
    private String nickname;
    private Integer number; // The number to guess
    private Integer score;
    private LocalDateTime lastActivity;
    private LocalDateTime lastUpdate;
    private Boolean active;
    private Boolean banned;

    // Tentativi effettuati oggi
    private int guessesToday = 0;
    private LocalDateTime guessesResetAt;
    
    // Default constructor
    public Player() {}
    
    // Constructor with basic fields
    public Player(String connectionId, String nickname) {
        this.connectionId = connectionId;
        this.nickname = nickname;
        this.score = 0;
        this.lastActivity = LocalDateTime.now();
        this.lastUpdate = LocalDateTime.now();
        this.active = true;
        this.banned = false;
    }
    
    // Getters and Setters
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
    
    public Integer getNumber() {
        return number;
    }
    
    public void setNumber(Integer number) {
        this.number = number;
    }
    
    public Integer getScore() {
        return score;
    }
    
    public void setScore(Integer score) {
        this.score = score;
    }
    
    public LocalDateTime getLastActivity() {
        return lastActivity;
    }
    
    public void setLastActivity(LocalDateTime lastActivity) {
        this.lastActivity = lastActivity;
    }
    
    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }
    
    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
    
    public Boolean getActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    public Boolean getBanned() {
        return banned;
    }
    
    public void setBanned(Boolean banned) {
        this.banned = banned;
    }

    public int getGuessesToday() {
        return guessesToday;
    }
    public void setGuessesToday(int guessesToday) {
        this.guessesToday = guessesToday;
    }
    public LocalDateTime getGuessesResetAt() {
        return guessesResetAt;
    }
    public void setGuessesResetAt(LocalDateTime guessesResetAt) {
        this.guessesResetAt = guessesResetAt;
    }
}
