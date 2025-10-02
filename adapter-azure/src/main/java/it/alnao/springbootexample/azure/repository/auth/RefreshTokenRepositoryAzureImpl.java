package it.alnao.springbootexample.azure.repository.auth;

import it.alnao.springbootexample.azure.entity.auth.RefreshTokenSqlServerEntity;
import it.alnao.springbootexample.core.domain.auth.RefreshToken;
import it.alnao.springbootexample.core.repository.auth.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Profile("azure")
public class RefreshTokenRepositoryAzureImpl implements RefreshTokenRepository {
    @Override
    @Transactional
    public void deleteByExpiryDateBefore(java.time.LocalDateTime dateTime) {
        jpaRepository.findAll().stream()
            .filter(e -> e.getExpiryDate() != null && e.getExpiryDate().isBefore(dateTime))
            .forEach(e -> jpaRepository.deleteById(e.getId()));
    }
    @Override
    public List<RefreshToken> findAll() {
        return jpaRepository.findAll().stream()
                .map(RefreshTokenSqlServerEntity::toDomain)
                .toList();
    }
    @Autowired
    private RefreshTokenSqlServerRepository jpaRepository;

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        if (refreshToken.getId() == null) {
            refreshToken.setId(UUID.randomUUID().toString());
        }
        RefreshTokenSqlServerEntity entity = RefreshTokenSqlServerEntity.fromDomain(refreshToken);
        RefreshTokenSqlServerEntity savedEntity = jpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return jpaRepository.findByToken(token)
                .map(RefreshTokenSqlServerEntity::toDomain);
    }

    @Override
    public List<RefreshToken> findByUserId(String userId) {
        return jpaRepository.findAll().stream()
                .filter(e -> userId.equals(e.getUserId()))
                .map(RefreshTokenSqlServerEntity::toDomain)
                .toList();
    }

    @Override
    public void delete(RefreshToken refreshToken) {
        jpaRepository.deleteById(refreshToken.getId());
    }

    @Override
    @Transactional
    public void deleteByToken(String token) {
        jpaRepository.findByToken(token).ifPresent(e -> jpaRepository.deleteById(e.getId()));
    }

    @Override
    @Transactional
    public void deleteByUserId(String userId) {
        jpaRepository.findAll().stream()
                .filter(e -> userId.equals(e.getUserId()))
                .forEach(e -> jpaRepository.deleteById(e.getId()));
    }
}
