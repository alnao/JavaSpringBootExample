package com.alnao.guessgame.repository;

import com.alnao.guessgame.model.MatchLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for MatchLog entity
 */
@Repository
public interface MatchLogRepository extends MongoRepository<MatchLog, String> {
    
    List<MatchLog> findByConnectionId(String connectionId);
    
    List<MatchLog> findByEvent(String event);
    
    @Query(value = "{}", sort = "{ 'timestamp': -1 }")
    List<MatchLog> findAllOrderByTimestampDesc();
    
    @Query("{ 'timestamp': { '$gte': ?0, '$lte': ?1 } }")
    List<MatchLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("{ 'event': ?0, 'timestamp': { '$gte': ?1 } }")
    List<MatchLog> findByEventAndTimestampAfter(String event, LocalDateTime after);
}
