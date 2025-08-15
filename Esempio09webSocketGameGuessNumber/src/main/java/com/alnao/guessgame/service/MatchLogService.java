package com.alnao.guessgame.service;

import com.alnao.guessgame.model.MatchLog;
import com.alnao.guessgame.repository.MatchLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing match logs
 */
@Service
public class MatchLogService {
    
    private static final Logger logger = LoggerFactory.getLogger(MatchLogService.class);
    
    @Autowired
    private MatchLogRepository matchLogRepository;
    
    public MatchLog logEvent(String event, String connectionId, String nickname, Object data) {
        logger.debug("Logging event: {} for player: {} ({})", event, nickname, connectionId);
        
        MatchLog matchLog = new MatchLog(event, connectionId, nickname, data);
        return matchLogRepository.save(matchLog);
    }
    
    public List<MatchLog> getAllLogs() {
        return matchLogRepository.findAllOrderByTimestampDesc();
    }
    
    public List<MatchLog> getLogsByConnectionId(String connectionId) {
        return matchLogRepository.findByConnectionId(connectionId);
    }
    
    public List<MatchLog> getLogsByEvent(String event) {
        return matchLogRepository.findByEvent(event);
    }
    
    public List<MatchLog> getLogsInTimeRange(LocalDateTime start, LocalDateTime end) {
        return matchLogRepository.findByTimestampBetween(start, end);
    }
    
    public List<MatchLog> getRecentLogs(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return matchLogRepository.findByTimestampBetween(since, LocalDateTime.now());
    }
}
