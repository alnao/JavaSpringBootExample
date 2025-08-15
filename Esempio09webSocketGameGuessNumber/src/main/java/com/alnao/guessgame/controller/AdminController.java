package com.alnao.guessgame.controller;

import com.alnao.guessgame.dto.ApiResponse;
import com.alnao.guessgame.model.MatchLog;
import com.alnao.guessgame.service.MatchLogService;
import com.alnao.guessgame.service.PlayerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for admin operations and logs
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    
    @Autowired
    private MatchLogService matchLogService;
    @Autowired
    private com.alnao.guessgame.service.WebSocketService webSocketService;
    @Autowired
    private PlayerService playerService;

    @GetMapping("/logs")
    public ResponseEntity<ApiResponse<List<MatchLog>>> getAllLogs() {
        try {
            List<MatchLog> logs = matchLogService.getAllLogs();
            return ResponseEntity.ok(ApiResponse.success(logs));
            
        } catch (Exception e) {
            logger.error("Error getting logs", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to get logs: " + e.getMessage()));
        }
    }
    
    @GetMapping("/logs/recent")
    public ResponseEntity<ApiResponse<List<MatchLog>>> getRecentLogs(
            @RequestParam(defaultValue = "24") int hours) {
        
        try {
            List<MatchLog> logs = matchLogService.getRecentLogs(hours);
            return ResponseEntity.ok(ApiResponse.success(logs));
            
        } catch (Exception e) {
            logger.error("Error getting recent logs", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to get recent logs: " + e.getMessage()));
        }
    }
    
    @GetMapping("/logs/by-event")
    public ResponseEntity<ApiResponse<List<MatchLog>>> getLogsByEvent(
            @RequestParam String event) {
        
        try {
            List<MatchLog> logs = matchLogService.getLogsByEvent(event);
            return ResponseEntity.ok(ApiResponse.success(logs));
            
        } catch (Exception e) {
            logger.error("Error getting logs by event", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to get logs by event: " + e.getMessage()));
        }
    }
    
    @GetMapping("/logs/by-timerange")
    public ResponseEntity<ApiResponse<List<MatchLog>>> getLogsByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        
        try {
            List<MatchLog> logs = matchLogService.getLogsInTimeRange(start, end);
            return ResponseEntity.ok(ApiResponse.success(logs));
            
        } catch (Exception e) {
            logger.error("Error getting logs by time range", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to get logs by time range: " + e.getMessage()));
        }
    }
    
    @GetMapping("/logs/by-player")
    public ResponseEntity<ApiResponse<List<MatchLog>>> getLogsByPlayer(
            @RequestParam String connectionId) {
        
        try {
            List<MatchLog> logs = matchLogService.getLogsByConnectionId(connectionId);
            return ResponseEntity.ok(ApiResponse.success(logs));
            
        } catch (Exception e) {
            logger.error("Error getting logs by player", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to get logs by player: " + e.getMessage()));
        }
    }

    @PostMapping("/broadcast")
    public ApiResponse<String> broadcast(@RequestBody java.util.Map<String, String> body) {
        String msg = body.get("message");
        if (msg == null || msg.trim().isEmpty()) {
            return ApiResponse.error("Message required");
        }
        webSocketService.broadcastAdminMessage(msg);
        return ApiResponse.success("Broadcast sent");
    }
    
    @PostMapping("/unban")
    public ResponseEntity<ApiResponse<String>> unbanPlayer(@RequestParam String nickname) {
        try {
            logger.info("Admin unbanning player: {}", nickname);
            playerService.unbanPlayer(nickname);
            return ResponseEntity.ok(ApiResponse.success("Player unbanned successfully"));
            
        } catch (Exception e) {
            logger.error("Error unbanning player", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to unban player: " + e.getMessage()));
        }
    }
}
