package com.alnao.guessgame.service;

import com.alnao.guessgame.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for WebSocket messaging
 */
@Service
public class WebSocketService {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketService.class);
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    
    public void broadcastPlayerJoined(Player player) {
        logger.info("Broadcasting player joined: {}", player.getNickname());
        
        Map<String, Object> message = new HashMap<>();
        message.put("type", "player_joined");
        message.put("player", player);
        
        messagingTemplate.convertAndSend("/topic/game", message);
    }
    
    public void broadcastPlayerLeft(Player player) {
        logger.info("Broadcasting player left: {}", player.getNickname());
        
        Map<String, Object> message = new HashMap<>();
        message.put("type", "player_left");
        message.put("player", player);
        
        messagingTemplate.convertAndSend("/topic/game", message);
    }
    
    public void broadcastSuccessfulGuess(Player guesser, Player target, Integer guess) {
        logger.info("Broadcasting successful guess: {} guessed {} from {}", 
            guesser.getNickname(), guess, target.getNickname());
        
        Map<String, Object> message = new HashMap<>();
        message.put("type", "successful_guess");
        message.put("guesser", guesser);
        message.put("target", target);
        message.put("number", guess);
        
        messagingTemplate.convertAndSend("/topic/game", message);
    }
    
    public void broadcastScoreUpdate(Player player) {
        logger.debug("Broadcasting score update for: {}", player.getNickname());
        
        Map<String, Object> message = new HashMap<>();
        message.put("type", "score_update");
        message.put("player", player);
        
        messagingTemplate.convertAndSend("/topic/game", message);
    }
    
    public void sendToPlayer(String connectionId, String type, Object data) {
        logger.debug("Sending message to player {}: {}", connectionId, type);
        
        Map<String, Object> message = new HashMap<>();
        message.put("type", type);
        message.put("data", data);
        
        messagingTemplate.convertAndSendToUser(connectionId, "/queue/personal", message);
    }
    
    public void notifyPlayerBanned(String connectionId) {
        logger.info("Notifying player banned: {}", connectionId);
        
        Map<String, Object> message = new HashMap<>();
        message.put("type", "banned");
        message.put("message", "You have been banned from the game");
        
        messagingTemplate.convertAndSendToUser(connectionId, "/queue/personal", message);
    }
    
    public void broadcastGameStats(int activePlayers, int totalMatches) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "game_stats");
        Map<String, Object> stats = new HashMap<>();
        stats.put("active_players", activePlayers);
        stats.put("total_matches", totalMatches);
        message.put("stats", stats);
        
        messagingTemplate.convertAndSend("/topic/game", message);
    }
    
    public void broadcastAdminMessage(String message) {
        logger.info("Broadcasting admin message: {}", message);
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", "admin_broadcast");
        msg.put("message", message);
        messagingTemplate.convertAndSend("/topic/game", msg);
    }
}
