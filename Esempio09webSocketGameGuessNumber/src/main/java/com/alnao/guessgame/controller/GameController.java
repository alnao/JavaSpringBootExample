package com.alnao.guessgame.controller;

import com.alnao.guessgame.dto.ApiResponse;
import com.alnao.guessgame.dto.GuessRequest;
import com.alnao.guessgame.dto.SetNumberRequest;
import com.alnao.guessgame.model.Player;
import com.alnao.guessgame.service.PlayerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

/**
 * REST Controller for game operations
 */
@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "*")
public class GameController {
    
    private static final Logger logger = LoggerFactory.getLogger(GameController.class);
    
    @Autowired
    private PlayerService playerService;
    
    /**
     * Helper method to check if a player is banned
     * @param connectionId the connection ID of the player
     * @return ResponseEntity with error if banned, null if not banned
     */
    private ResponseEntity<ApiResponse<String>> checkPlayerBanned(String connectionId) {
        Optional<Player> playerOpt = playerService.getPlayer(connectionId);
        if (playerOpt.isPresent() && playerOpt.get().getBanned()) {
            logger.warn("Banned player {} attempted to perform an action", connectionId);
            return ResponseEntity.status(403)
                .body(ApiResponse.error("You are banned from the game"));
        }
        return null;
    }
    
    @PostMapping("/join")
    public ResponseEntity<ApiResponse<Player>> joinGame(
            @RequestParam String connectionId,
            @RequestParam String nickname) {
        try {
            logger.info("Player joining game: {} with connectionId: {}", nickname, connectionId);

            // Se il nickname esiste e il player è attivo, aggiorna solo il connectionId (login)
            Optional<Player> existingPlayer = playerService.getPlayerByNickname(nickname);
            if (existingPlayer.isPresent()) {
                Player player = existingPlayer.get();
                
                // Controlla se il player è bannato
                if (player.getBanned()) {
                    logger.warn("Banned player {} attempted to join with connectionId: {}", nickname, connectionId);
                    return ResponseEntity.status(403)
                        .body(ApiResponse.error("You are banned from the game"));
                }
                
                // Aggiorna il connectionId e imposta attivo
                playerService.updatePlayerConnectionIdAndActive(player, connectionId, true);
                return ResponseEntity.ok(ApiResponse.success("Player logged in successfully", player));
            }

            // Se non esiste, crea nuovo player
            Player player = playerService.createPlayer(connectionId, nickname);
            return ResponseEntity.ok(ApiResponse.success("Player joined successfully", player));
        } catch (Exception e) {
            logger.error("Error joining game", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to join game: " + e.getMessage()));
        }
    }
    
    @PostMapping("/set-number")
    public ResponseEntity<ApiResponse<Player>> setNumber(
            @RequestParam String connectionId,
            @RequestBody SetNumberRequest request) {
        
        try {
            logger.info("Setting number for player: {}", connectionId);
            
            // Check if player is banned
            ResponseEntity<ApiResponse<String>> banCheck = checkPlayerBanned(connectionId);
            if (banCheck != null) {
                return ResponseEntity.status(banCheck.getStatusCode())
                    .body(ApiResponse.error(banCheck.getBody().getMessage()));
            }
            
            if (request.getNumber() == null || request.getNumber() < 1 || request.getNumber() > 100) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Number must be between 1 and 100"));
            }
            
            Player player = playerService.setPlayerNumber(connectionId, request.getNumber());
            if (player == null) {
                return ResponseEntity.notFound()
                    .build();
            }
            
            return ResponseEntity.ok(ApiResponse.success("Number set successfully", player));
            
        } catch (Exception e) {
            logger.error("Error setting number", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to set number: " + e.getMessage()));
        }
    }
    
    @PostMapping("/guess")
    public ResponseEntity<ApiResponse<String>> makeGuess(
            @RequestParam String connectionId,
            @RequestBody GuessRequest request) {
        
        try {
            logger.info("Player {} making guess: {}", connectionId, request.getGuess());
            
            // Check if player is banned
            ResponseEntity<ApiResponse<String>> banCheck = checkPlayerBanned(connectionId);
            if (banCheck != null) {
                return banCheck;
            }
            
            if (request.getGuess() == null || request.getGuess() < 1 || request.getGuess() > 100) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Guess must be between 1 and 100"));
            }
            
            String result = playerService.makeGuess(connectionId, request.getGuess());
            return ResponseEntity.ok(ApiResponse.success("Guess processed", result));
            
        } catch (Exception e) {
            logger.error("Error making guess", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to process guess: " + e.getMessage()));
        }
    }
    
    @GetMapping("/scores")
    public ResponseEntity<ApiResponse<List<Player>>> getScores() {
        try {
            List<Player> topScorers = playerService.getTopScorers();
            return ResponseEntity.ok(ApiResponse.success(topScorers));
            
        } catch (Exception e) {
            logger.error("Error getting scores", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to get scores: " + e.getMessage()));
        }
    }
    
    @GetMapping("/active-players")
    public ResponseEntity<ApiResponse<List<Player>>> getActivePlayers() {
        try {
            List<Player> activePlayers = playerService.getActivePlayers();
            return ResponseEntity.ok(ApiResponse.success(activePlayers));
            
        } catch (Exception e) {
            logger.error("Error getting active players", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to get active players: " + e.getMessage()));
        }
    }
    
    @PostMapping("/disconnect")
    public ResponseEntity<ApiResponse<String>> disconnect(@RequestParam String connectionId) {
        try {
            playerService.disconnectPlayer(connectionId);
            return ResponseEntity.ok(ApiResponse.success("Player disconnected successfully"));
            
        } catch (Exception e) {
            logger.error("Error disconnecting player", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to disconnect player: " + e.getMessage()));
        }
    }
    
    @PostMapping("/ban")
    public ResponseEntity<ApiResponse<String>> banPlayer(@RequestParam String connectionId) {
        try {
            playerService.banPlayer(connectionId);
            return ResponseEntity.ok(ApiResponse.success("Player banned successfully"));
            
        } catch (Exception e) {
            logger.error("Error banning player", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to ban player: " + e.getMessage()));
        }
    }
    
    @PostMapping("/cleanup")
    public ResponseEntity<ApiResponse<String>> cleanupInactivePlayers(
            @RequestParam(defaultValue = "30") int inactivityMinutes) {
        
        try {
            playerService.cleanupInactivePlayers(inactivityMinutes);
            return ResponseEntity.ok(ApiResponse.success("Cleanup completed"));
            
        } catch (Exception e) {
            logger.error("Error during cleanup", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to cleanup: " + e.getMessage()));
        }
    }
    
    @GetMapping("/banned-players")
    public ResponseEntity<ApiResponse<List<Player>>> getBannedPlayers() {
        try {
            List<Player> banned = playerService.getBannedPlayers();
            return ResponseEntity.ok(ApiResponse.success(banned));
        } catch (Exception e) {
            logger.error("Error getting banned players", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to get banned players: " + e.getMessage()));
        }
    }
}
