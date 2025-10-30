package it.alnao.springbootexample.postgresql.repository.auth;

import it.alnao.springbootexample.postgresql.entity.auth.RefreshTokenEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository JPA per PostgreSQL dei refresh token.
 */
@Repository
public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenEntity, String> {
    
    Optional<RefreshTokenEntity> findByToken(String token);
    
    List<RefreshTokenEntity> findByUserId(String userId);
    
    void deleteByToken(String token);
    
    void deleteByUserId(String userId);
    
    void deleteByExpiryDateBefore(LocalDateTime dateTime);
}
