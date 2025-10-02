package it.alnao.springbootexample.azure.repository.auth;

import it.alnao.springbootexample.azure.entity.auth.RefreshTokenSqlServerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenSqlServerRepository extends JpaRepository<RefreshTokenSqlServerEntity, String> {
    Optional<RefreshTokenSqlServerEntity> findByToken(String token);
}
