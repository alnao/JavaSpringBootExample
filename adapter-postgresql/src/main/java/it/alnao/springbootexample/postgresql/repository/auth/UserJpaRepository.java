package it.alnao.springbootexample.postgresql.repository.auth;

import it.alnao.springbootexample.postgresql.entity.auth.UserEntity;
import it.alnao.springbootexample.core.domain.auth.AccountType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository JPA per PostgreSQL degli utenti.
 */
@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, String> {
    
    Optional<UserEntity> findByUsername(String username);
    
    Optional<UserEntity> findByEmail(String email);
    
    Optional<UserEntity> findByEmailAndAccountType(String email, AccountType accountType);
    
    Optional<UserEntity> findByExternalIdAndAccountType(String externalId, AccountType accountType);
    
    boolean existsByEmail(String email);
    
    boolean existsByUsername(String username);
    
    List<UserEntity> findByAccountType(AccountType accountType);
    
    List<UserEntity> findByEnabled(boolean enabled);
    
    long countByAccountType(AccountType accountType);
    
    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.emailVerified = :verified")
    long countByEmailVerified(@Param("verified") boolean verified);
    
    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.enabled = :enabled")
    long countByEnabled(@Param("enabled") boolean enabled);
    
    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.providers WHERE u.id = :id")
    Optional<UserEntity> findByIdWithProviders(@Param("id") String id);
}
