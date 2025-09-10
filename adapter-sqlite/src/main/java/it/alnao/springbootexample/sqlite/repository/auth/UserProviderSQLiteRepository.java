package it.alnao.springbootexample.sqlite.repository.auth;

import it.alnao.springbootexample.sqlite.entity.auth.UserProviderSQLiteEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository JPA per UserProvider su SQLite.
 */
@Repository
@Profile("sqlite")
public interface UserProviderSQLiteRepository extends JpaRepository<UserProviderSQLiteEntity, String> {
    
    // Query personalizzate solo con entità
    @Query("SELECT up FROM UserProviderSQLiteEntity up WHERE up.userId = :userId")
    List<UserProviderSQLiteEntity> findByUserId(@Param("userId") String userId);

    @Query("SELECT up FROM UserProviderSQLiteEntity up WHERE up.provider = :provider")
    List<UserProviderSQLiteEntity> findByProvider(@Param("provider") String provider);

    @Query("SELECT up FROM UserProviderSQLiteEntity up WHERE up.externalId = :providerId")
    Optional<UserProviderSQLiteEntity> findByExternalId(@Param("providerId") String providerId);

    @Query("SELECT up FROM UserProviderSQLiteEntity up WHERE up.provider = :provider AND up.externalId = :providerId")
    Optional<UserProviderSQLiteEntity> findByProviderAndExternalId(@Param("provider") String provider, @Param("providerId") String providerId);

    @Query("SELECT up FROM UserProviderSQLiteEntity up WHERE up.userId = :userId AND up.provider = :provider")
    Optional<UserProviderSQLiteEntity> findByUserIdAndProvider(@Param("userId") String userId, @Param("provider") String provider);

    @Query("SELECT up FROM UserProviderSQLiteEntity up WHERE up.providerEmail = :email")
    List<UserProviderSQLiteEntity> findByProviderEmail(@Param("email") String email);

    @Query("SELECT up FROM UserProviderSQLiteEntity up WHERE up.provider = :provider AND up.providerEmail = :email")
    Optional<UserProviderSQLiteEntity> findByProviderAndProviderEmail(@Param("provider") String provider, @Param("email") String email);

    @Query("SELECT DISTINCT up.provider FROM UserProviderSQLiteEntity up")
    List<String> findDistinctProviders();
}
