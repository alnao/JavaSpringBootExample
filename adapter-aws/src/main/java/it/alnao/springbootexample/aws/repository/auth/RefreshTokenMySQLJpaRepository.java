package it.alnao.springbootexample.aws.repository.auth;

import it.alnao.springbootexample.aws.entity.auth.RefreshTokenMySQLEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * MySQL repository for refresh tokens (AWS profile).
 */
@Repository
@Profile("aws")
public interface RefreshTokenMySQLJpaRepository extends JpaRepository<RefreshTokenMySQLEntity, String> {
    Optional<RefreshTokenMySQLEntity> findByToken(String token);
    List<RefreshTokenMySQLEntity> findByUserId(String userId);
    void deleteByToken(String token);
    void deleteByUserId(String userId);
    void deleteByExpiryDateBefore(LocalDateTime dateTime);
}
