package it.alnao.springbootexample.azure.repository.auth;

import it.alnao.springbootexample.azure.entity.auth.UserSqlServerEntity;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Profile("azure")
public interface UserSqlServerJpaRepository extends JpaRepository<UserSqlServerEntity, String> {
    Optional<UserSqlServerEntity> findByUsername(String username);

    Optional<UserSqlServerEntity> findByEmail(String email);

    @Query("SELECT u FROM UserSqlServerEntity u WHERE u.email = :email AND u.accountType = :accountType")
    Optional<UserSqlServerEntity> findByEmailAndAccountType(@Param("email") String email,
                                                         @Param("accountType") String accountType);

    @Query("SELECT u FROM UserSqlServerEntity u WHERE u.externalId = :externalId AND u.accountType = :accountType")
    Optional<UserSqlServerEntity> findByExternalIdAndAccountType(@Param("externalId") String externalId,
                                                              @Param("accountType") String accountType);
    
    boolean existsByEmail(String email);
    
    boolean existsByUsername(String username);
    
    List<UserSqlServerEntity> findByAccountType(String accountType);
    
    List<UserSqlServerEntity> findByEnabled(boolean enabled);
    
    long countByAccountType(String accountType);
}

/*
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

 */