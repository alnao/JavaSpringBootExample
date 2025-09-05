package it.alnao.springbootexample.aws.repository.auth;

import it.alnao.springbootexample.aws.entity.auth.UserProviderMySQLEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MySQL repository for user OAuth2 providers (AWS profile).
 */
@Repository
@Profile("aws")
public interface UserProviderMySQLJpaRepository extends JpaRepository<UserProviderMySQLEntity, String> {
    List<UserProviderMySQLEntity> findByUserId(String userId);
    Optional<UserProviderMySQLEntity> findByUserIdAndProvider(String userId, String provider);
    Optional<UserProviderMySQLEntity> findByProviderAndProviderUserId(String provider, String providerUserId);
    boolean existsByUserIdAndProvider(String userId, String provider);
    void deleteByUserId(String userId);
    void deleteByUserIdAndProvider(String userId, String provider);
}
