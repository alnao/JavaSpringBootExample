package com.alnao.guessgame.repository;

import com.alnao.guessgame.model.Player;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Player entity
 */
@Repository
public interface PlayerRepository extends MongoRepository<Player, String> {
    
    Optional<Player> findByNickname(String nickname);
    
    List<Player> findByActiveTrue();
    
    List<Player> findByBannedTrue();
    
    @Query("{ 'active': true }")
    List<Player> findActivePlayers();
    
    @Query(value = "{ 'active': true }", sort = "{ 'score': -1 }")
    List<Player> findActivePlayersOrderByScoreDesc();
    
    @Query("{ 'lastActivity': { '$lt': ?0 } }")
    List<Player> findPlayersInactiveSince(LocalDateTime threshold);
    
    @Query("{ 'active': true, 'score': { '$gt': 0 } }")
    List<Player> findActivePlayersWithPositiveScore();
}
