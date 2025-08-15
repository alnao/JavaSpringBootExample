package com.alnao.guessgame.service;

import com.alnao.guessgame.config.GameScoringConfig;
import com.alnao.guessgame.model.Player;
import com.alnao.guessgame.repository.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing players
 */
@Service
public class PlayerService {
    
    private static final Logger logger = LoggerFactory.getLogger(PlayerService.class);
    
    @Autowired
    private PlayerRepository playerRepository;
    
    @Autowired
    private WebSocketService webSocketService;
    
    @Autowired
    private MatchLogService matchLogService;
    
    @Autowired
    private GameScoringConfig scoringConfig;
    
    public Player createPlayer(String connectionId, String nickname) {
        logger.info("Creating player with connectionId: {} and nickname: {}", connectionId, nickname);
        
        Player player = new Player(connectionId, nickname);
        Player savedPlayer = playerRepository.save(player);
        
        // Log the event
        matchLogService.logEvent("PLAYER_CONNECTED", connectionId, nickname, null);
        
        // Broadcast to all players
        webSocketService.broadcastPlayerJoined(savedPlayer);
        
        return savedPlayer;
    }
    
    public Optional<Player> getPlayer(String connectionId) {
        return playerRepository.findById(connectionId);
    }
    
    public Optional<Player> getPlayerByNickname(String nickname) {
        return playerRepository.findByNickname(nickname);
    }
    
    public List<Player> getActivePlayers() {
        return playerRepository.findByActiveTrue();
    }
    
    public List<Player> getTopScorers() {
        return playerRepository.findActivePlayersOrderByScoreDesc();
    }
    
    public Player updatePlayerActivity(String connectionId) {
        Optional<Player> playerOpt = playerRepository.findById(connectionId);
        if (playerOpt.isPresent()) {
            Player player = playerOpt.get();
            player.setLastActivity(LocalDateTime.now());
            return playerRepository.save(player);
        }
        return null;
    }
    
    public Player setPlayerNumber(String connectionId, Integer number) {
        logger.info("Setting number {} for player {}", number, connectionId);
        
        Optional<Player> playerOpt = playerRepository.findById(connectionId);
        if (playerOpt.isPresent()) {
            Player player = playerOpt.get();
            
            // Check if player is banned
            if (player.getBanned()) {
                logger.warn("Banned player {} attempted to set number", connectionId);
                return null;
            }
            
            player.setNumber(number);
            player.setLastUpdate(LocalDateTime.now());
            player.setLastActivity(LocalDateTime.now());
            
            Player savedPlayer = playerRepository.save(player);
            
            // Log the event
            matchLogService.logEvent("NUMBER_SET", connectionId, player.getNickname(), number);
            
            return savedPlayer;
        }
        return null;
    }
    
    public String makeGuess(String connectionId, Integer guess) {
        logger.info("Player {} making guess: {}", connectionId, guess);
        
        Optional<Player> guesserOpt = playerRepository.findById(connectionId);
        if (!guesserOpt.isPresent()) {
            return "Player not found";
        }
        
        Player guesser = guesserOpt.get();
        
        // Check if player is banned
        if (guesser.getBanned()) {
            logger.warn("Banned player {} attempted to make guess", connectionId);
            return "You are banned from the game";
        }
        
        // Rate limit: max tentativi ogni 24h
        int maxGuesses = scoringConfig.getMaxGuessesPerDay();
        LocalDateTime now = LocalDateTime.now();
        if (guesser.getGuessesResetAt() == null || now.isAfter(guesser.getGuessesResetAt())) {
            guesser.setGuessesToday(0);
            guesser.setGuessesResetAt(now.plusHours(24));
        }
        if (guesser.getGuessesToday() >= maxGuesses) {
            return "Guess limit reached for today (max " + maxGuesses + ")";
        }
        guesser.setGuessesToday(guesser.getGuessesToday() + 1);
        
        // Find all players with numbers set
        List<Player> playersWithNumbers = playerRepository.findActivePlayers()
            .stream()
            .filter(p -> p.getNumber() != null && !p.getConnectionId().equals(connectionId))
            .toList();
        
        String result = "no_match";
        
        for (Player target : playersWithNumbers) {
            if (target.getNumber().equals(guess)) {
                // Match found!
                guesser.setScore(guesser.getScore() + scoringConfig.getGuessCorrect());
                target.setScore(target.getScore() + scoringConfig.getTargetFound());
                
                // Reset target's number
                target.setNumber(null);
                target.setLastUpdate(LocalDateTime.now());
                
                playerRepository.save(guesser);
                playerRepository.save(target);
                
                // Log the successful guess
                matchLogService.logEvent("SUCCESSFUL_GUESS", connectionId, guesser.getNickname(), 
                    String.format("Guessed %d from %s", guess, target.getNickname()));
                
                // Broadcast the match
                webSocketService.broadcastSuccessfulGuess(guesser, target, guess);
                
                result = "match";
                break;
            }
        }
        
        if ("no_match".equals(result)) {
            // Apply penalty for wrong guess
            guesser.setScore(Math.max(0, guesser.getScore() - scoringConfig.getGuessWrongPenalty()));
            guesser.setLastActivity(LocalDateTime.now());
            playerRepository.save(guesser);
            
            // Log the failed guess
            matchLogService.logEvent("FAILED_GUESS", connectionId, guesser.getNickname(), guess);
        }
        
        return result;
    }
    
    public void disconnectPlayer(String connectionId) {
        logger.info("Disconnecting player: {}", connectionId);
        
        Optional<Player> playerOpt = playerRepository.findById(connectionId);
        if (playerOpt.isPresent()) {
            Player player = playerOpt.get();
            player.setActive(false);
            playerRepository.save(player);
            
            // Log the event
            matchLogService.logEvent("PLAYER_DISCONNECTED", connectionId, player.getNickname(), null);
            
            // Broadcast to all players
            webSocketService.broadcastPlayerLeft(player);
        }
    }
    
    public void banPlayer(String connectionId) {
        logger.info("Banning player: {}", connectionId);
        
        Optional<Player> playerOpt = playerRepository.findById(connectionId);
        if (playerOpt.isPresent()) {
            Player player = playerOpt.get();
            player.setBanned(true);
            player.setActive(false);
            playerRepository.save(player);
            
            // Log the event
            matchLogService.logEvent("PLAYER_BANNED", connectionId, player.getNickname(), null);
            
            // Notify the player and disconnect
            webSocketService.notifyPlayerBanned(connectionId);
        }
    }
    
    public void unbanPlayer(String nickname) {
        logger.info("Unbanning player: {}", nickname);
        
        Optional<Player> playerOpt = playerRepository.findByNickname(nickname);
        if (playerOpt.isPresent()) {
            Player player = playerOpt.get();
            player.setBanned(false);
            playerRepository.save(player);
            
            // Log the event
            matchLogService.logEvent("PLAYER_UNBANNED", player.getConnectionId(), player.getNickname(), null);
            
            logger.info("Player {} has been unbanned", nickname);
        }
    }
    
    public void cleanupInactivePlayers(int inactivityMinutes) {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(inactivityMinutes);
        List<Player> inactivePlayers = playerRepository.findPlayersInactiveSince(threshold);
        
        for (Player player : inactivePlayers) {
            if (player.getActive()) {
                player.setActive(false);
                playerRepository.save(player);
                
                matchLogService.logEvent("PLAYER_CLEANUP", player.getConnectionId(), 
                    player.getNickname(), "Inactive for " + inactivityMinutes + " minutes");
            }
        }
        
        logger.info("Cleaned up {} inactive players", inactivePlayers.size());
    }
    
    /**
     * Aggiorna il connectionId e lo stato attivo di un player (login), gestendo la chiave primaria
     * Fix: elimina prima il vecchio player, poi salva il nuovo per evitare errori di indice unico su nickname
     */
    public Player updatePlayerConnectionIdAndActive(Player player, String newConnectionId, boolean active) {
        String oldConnectionId = player.getConnectionId();
        if (!oldConnectionId.equals(newConnectionId)) {
            // Elimina prima il vecchio player
            playerRepository.deleteById(oldConnectionId);
            // Crea nuovo player con nuovo connectionId, copia i dati
            Player updated = new Player(newConnectionId, player.getNickname());
            updated.setScore(player.getScore());
            updated.setNumber(player.getNumber());
            updated.setActive(active);
            updated.setBanned(player.getBanned());
            updated.setLastActivity(LocalDateTime.now());
            updated.setLastUpdate(LocalDateTime.now());
            // Salva nuovo player
            Player saved = playerRepository.save(updated);
            // Logga l'evento di login
            matchLogService.logEvent("PLAYER_LOGIN", newConnectionId, player.getNickname(), null);
            webSocketService.broadcastPlayerJoined(saved);
            return saved;
        } else {
            // Solo aggiorna stato attivo e timestamp
            player.setActive(active);
            player.setLastActivity(LocalDateTime.now());
            player.setLastUpdate(LocalDateTime.now());
            Player saved = playerRepository.save(player);
            matchLogService.logEvent("PLAYER_LOGIN", newConnectionId, player.getNickname(), null);
            webSocketService.broadcastPlayerJoined(saved);
            return saved;
        }
    }
    
    public List<Player> getBannedPlayers() {
        return playerRepository.findByBannedTrue();
    }
}
