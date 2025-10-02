package it.alnao.springbootexample.azure.repository.auth;

import it.alnao.springbootexample.azure.entity.auth.UserSqlServerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSqlServerRepository extends JpaRepository<UserSqlServerEntity, String> {
    Optional<UserSqlServerEntity> findByUsername(String username);
    Optional<UserSqlServerEntity> findByEmail(String email);
}
