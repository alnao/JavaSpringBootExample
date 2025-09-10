package it.alnao.springbootexample.sqlite.repository.auth;

import it.alnao.springbootexample.port.domain.auth.RefreshToken;
import it.alnao.springbootexample.port.repository.auth.RefreshTokenRepository;
import it.alnao.springbootexample.sqlite.entity.auth.RefreshTokenSQLiteEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository JPA per RefreshToken su SQLite.
 */
@Repository
@Profile("sqlite")
public interface RefreshTokenSQLiteRepository extends JpaRepository<RefreshTokenSQLiteEntity, String> {
    
    // Query personalizzate solo con entità
    @Query("SELECT r FROM RefreshTokenSQLiteEntity r WHERE r.token = :token")
    Optional<RefreshTokenSQLiteEntity> findByToken(@Param("token") String token);

    @Query("SELECT r FROM RefreshTokenSQLiteEntity r WHERE r.userId = :userId")
    List<RefreshTokenSQLiteEntity> findByUserId(@Param("userId") String userId);
    
//    @Query("SELECT r FROM RefreshTokenSQLiteEntity r WHERE r.userId = :userId AND r.valido = true")
//    List<RefreshTokenSQLiteEntity> findByUserIdAndValidoTrue(@Param("userId") String userId);
    
    
//    @Query("SELECT r FROM RefreshTokenSQLiteEntity r WHERE r.dataScadenza < :now")
//    List<RefreshTokenSQLiteEntity> findByDataScadenzaBefore(@Param("now") LocalDateTime now);
    
    
//    @Query("SELECT r FROM RefreshTokenSQLiteEntity r WHERE r.valido = true AND r.dataScadenza > :now")
    //List<RefreshTokenSQLiteEntity> findValidTokens(@Param("now") LocalDateTime now);
    
/*
    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshTokenSQLiteEntity r WHERE r.dataScadenza < :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);
   */      
    /*
    @Modifying
    @Transactional
    @Query("UPDATE RefreshTokenSQLiteEntity r SET r.valido = false WHERE r.userId = :userId")
    int invalidateUserTokens(@Param("userId") String userId);
    
    
    @Modifying
    @Transactional
    @Query("UPDATE RefreshTokenSQLiteEntity r SET r.valido = false WHERE r.token = :token")
    int invalidateToken(@Param("token") String token);
     */
}