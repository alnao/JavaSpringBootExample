package com.alnao.guessgame.websocket;

import com.alnao.guessgame.service.PlayerService;
import com.alnao.guessgame.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;

/**
 * WebSocket message handler
 */
@Controller
public class WebSocketController {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);
    
    @Autowired
    private PlayerService playerService;
    
    /**
     * Helper method to check if a player is banned
     * @param connectionId the connection ID of the player
     * @return Map with error if banned, null if not banned
     */
    private Map<String, Object> checkPlayerBanned(String connectionId) {
        Optional<Player> playerOpt = playerService.getPlayer(connectionId);
        if (playerOpt.isPresent() && playerOpt.get().getBanned()) {
            logger.warn("Banned player {} attempted WebSocket action", connectionId);
            return Map.of("type", "banned_error", "message", "You are banned from the game");
        }
        return null;
    }
    
    @MessageMapping("/game/join")
    @SendTo("/topic/game")
    public Map<String, Object> handleJoinGame(Map<String, Object> message, Principal principal) {
        String connectionId = (principal != null) ? principal.getName() : null;
        String nickname = (String) message.get("nickname");

        if (connectionId == null) {
            logger.warn("WebSocket join request with null principal (unauthenticated connection)");
            return Map.of("type", "join_error", "message", "Unauthenticated WebSocket connection");
        }

        logger.info("WebSocket join request from {}: {}", connectionId, nickname);

        try {
            // Check if the player is banned
            Map<String, Object> banCheck = checkPlayerBanned(connectionId);
            if (banCheck != null) {
                return banCheck;
            }
            
            playerService.createPlayer(connectionId, nickname);
            return Map.of("type", "join_success", "message", "Joined successfully");
        } catch (Exception e) {
            logger.error("Error in WebSocket join", e);
            return Map.of("type", "join_error", "message", e.getMessage());
        }
    }
    
    @MessageMapping("/game/ping")
    @SendToUser("/queue/personal")
    public Map<String, Object> handlePing(Principal principal) {
        String connectionId = (principal != null) ? principal.getName() : null;

        if (connectionId == null) {
            logger.warn("WebSocket ping with null principal (unauthenticated connection)");
            return Map.of("type", "pong_error", "message", "Unauthenticated WebSocket connection");
        }

        // Update player activity
        playerService.updatePlayerActivity(connectionId);

        return Map.of("type", "pong", "timestamp", System.currentTimeMillis());
    }
    
    @MessageMapping("/game/set-number")
    @SendToUser("/queue/personal")
    public Map<String, Object> handleSetNumber(Map<String, Object> message, Principal principal) {
        String connectionId = principal.getName();
        Integer number = (Integer) message.get("number");
        
        logger.info("WebSocket set number request from {}: {}", connectionId, number);
        
        try {
            // Check if the player is banned
            Map<String, Object> banCheck = checkPlayerBanned(connectionId);
            if (banCheck != null) {
                return banCheck;
            }
            
            if (number == null || number < 1 || number > 100) {
                return Map.of("type", "set_number_error", "message", "Number must be between 1 and 100");
            }
            
            playerService.setPlayerNumber(connectionId, number);
            return Map.of("type", "set_number_success", "message", "Number set successfully");
        } catch (Exception e) {
            logger.error("Error in WebSocket set number", e);
            return Map.of("type", "set_number_error", "message", e.getMessage());
        }
    }
    
    @MessageMapping("/game/guess")
    @SendToUser("/queue/personal")
    public Map<String, Object> handleGuess(Map<String, Object> message, Principal principal) {
        String connectionId = principal.getName();
        Integer guess = (Integer) message.get("guess");
        
        logger.info("WebSocket guess request from {}: {}", connectionId, guess);
        
        try {
            // Check if the player is banned
            Map<String, Object> banCheck = checkPlayerBanned(connectionId);
            if (banCheck != null) {
                return banCheck;
            }
            
            if (guess == null || guess < 1 || guess > 100) {
                return Map.of("type", "guess_error", "message", "Guess must be between 1 and 100");
            }
            
            String result = playerService.makeGuess(connectionId, guess);
            return Map.of("type", "guess_result", "result", result);
        } catch (Exception e) {
            logger.error("Error in WebSocket guess", e);
            return Map.of("type", "guess_error", "message", e.getMessage());
        }
    }
}
