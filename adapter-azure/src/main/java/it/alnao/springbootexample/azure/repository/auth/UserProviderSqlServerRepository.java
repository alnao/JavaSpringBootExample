package it.alnao.springbootexample.azure.repository.auth;

import it.alnao.springbootexample.azure.entity.auth.UserProviderSqlServerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProviderSqlServerRepository extends JpaRepository<UserProviderSqlServerEntity, String> {
    List<UserProviderSqlServerEntity> findByUserId(String userId);
    Optional<UserProviderSqlServerEntity> findByProviderAndProviderUserId(String provider, String providerUserId);
}
