package it.alnao.springbootexample.postgresql.repository.auth;

import it.alnao.springbootexample.postgresql.entity.auth.UserProviderEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository JPA per PostgreSQL dei provider OAuth2.
 */
@Repository
public interface UserProviderJpaRepository extends JpaRepository<UserProviderEntity, String> {
    
    List<UserProviderEntity> findByUserId(String userId);
    
    Optional<UserProviderEntity> findByUserIdAndProvider(String userId, String provider);
    
    Optional<UserProviderEntity> findByProviderAndProviderUserId(String provider, String providerUserId);
    
    boolean existsByUserIdAndProvider(String userId, String provider);
    
    void deleteByUserId(String userId);
    
    void deleteByUserIdAndProvider(String userId, String provider);
}
