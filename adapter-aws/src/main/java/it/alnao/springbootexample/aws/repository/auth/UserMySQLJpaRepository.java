package it.alnao.springbootexample.aws.repository.auth;

import it.alnao.springbootexample.aws.entity.auth.UserMySQLEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository JPA per utenti su MySQL AWS.
 */
@Repository
@Profile("aws")
public interface UserMySQLJpaRepository extends JpaRepository<UserMySQLEntity, String> {
    
    Optional<UserMySQLEntity> findByUsername(String username);
    
    Optional<UserMySQLEntity> findByEmail(String email);
    
    @Query("SELECT u FROM UserMySQLEntity u WHERE u.email = :email AND u.accountType = :accountType")
    Optional<UserMySQLEntity> findByEmailAndAccountType(@Param("email") String email, 
                                                         @Param("accountType") String accountType);
    
    @Query("SELECT u FROM UserMySQLEntity u WHERE u.externalId = :externalId AND u.accountType = :accountType")
    Optional<UserMySQLEntity> findByExternalIdAndAccountType(@Param("externalId") String externalId, 
                                                              @Param("accountType") String accountType);
    
    boolean existsByEmail(String email);
    
    boolean existsByUsername(String username);
    
    List<UserMySQLEntity> findByAccountType(String accountType);
    
    List<UserMySQLEntity> findByEnabled(boolean enabled);
    
    long countByAccountType(String accountType);
}
