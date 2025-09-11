package it.alnao.springbootexample.aws.repository.auth;

import it.alnao.springbootexample.aws.entity.auth.RefreshTokenMySQLEntity;
import it.alnao.springbootexample.core.domain.auth.RefreshToken;
import it.alnao.springbootexample.core.repository.auth.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementazione DynamoDB del repository refresh token per AWS.
 */
@Repository
@Profile("aws")
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {
    @Autowired
    private RefreshTokenMySQLJpaRepository jpaRepository;

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        if (refreshToken.getId() == null) {
            refreshToken.setId(UUID.randomUUID().toString());
        }
        RefreshTokenMySQLEntity entity = RefreshTokenMySQLEntity.fromDomain(refreshToken);
        RefreshTokenMySQLEntity savedEntity = jpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return jpaRepository.findByToken(token)
                .map(RefreshTokenMySQLEntity::toDomain);
    }

    @Override
    public List<RefreshToken> findByUserId(String userId) {
        return jpaRepository.findByUserId(userId).stream()
                .map(RefreshTokenMySQLEntity::toDomain)
                .toList();
    }

    @Override
    public void delete(RefreshToken refreshToken) {
        jpaRepository.deleteById(refreshToken.getId());
    }

    @Override
    @Transactional
    public void deleteByToken(String token) {
        jpaRepository.deleteByToken(token);
    }

    @Override
    @Transactional
    public void deleteByUserId(String userId) {
        jpaRepository.deleteByUserId(userId);
    }

    @Override
    @Transactional
    public void deleteByExpiryDateBefore(LocalDateTime dateTime) {
        jpaRepository.deleteByExpiryDateBefore(dateTime);
    }

    @Override
    public List<RefreshToken> findAll() {
        return jpaRepository.findAll().stream()
                .map(RefreshTokenMySQLEntity::toDomain)
                .toList();
    }
}
