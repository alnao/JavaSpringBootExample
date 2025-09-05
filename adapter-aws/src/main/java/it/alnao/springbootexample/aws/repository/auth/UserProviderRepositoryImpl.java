package it.alnao.springbootexample.aws.repository.auth;

import it.alnao.springbootexample.aws.entity.auth.UserProviderMySQLEntity;
import it.alnao.springbootexample.port.domain.auth.UserProvider;
import it.alnao.springbootexample.port.repository.auth.UserProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementazione UserProviderMySQLEntity del repository provider OAuth2 per AWS.
 */
@Repository
@Profile("aws")
public class UserProviderRepositoryImpl implements UserProviderRepository {

    @Autowired
    private UserProviderMySQLJpaRepository jpaRepository;

    @Override
    public UserProvider save(UserProvider userProvider) {
        if (userProvider.getId() == null) {
            userProvider.setId(UUID.randomUUID().toString());
        }
        UserProviderMySQLEntity entity = UserProviderMySQLEntity.fromDomain(userProvider);
        UserProviderMySQLEntity savedEntity = jpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<UserProvider> findById(String id) {
        return jpaRepository.findById(id)
                .map(UserProviderMySQLEntity::toDomain);
    }

    @Override
    public List<UserProvider> findByUserId(String userId) {
        return jpaRepository.findByUserId(userId).stream()
                .map(UserProviderMySQLEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<UserProvider> findByUserIdAndProvider(String userId, String provider) {
        return jpaRepository.findByUserIdAndProvider(userId, provider)
                .map(UserProviderMySQLEntity::toDomain);
    }

    @Override
    public Optional<UserProvider> findByProviderAndProviderUserId(String provider, String providerUserId) {
        return jpaRepository.findByProviderAndProviderUserId(provider, providerUserId)
                .map(UserProviderMySQLEntity::toDomain);
    }

    @Override
    public boolean existsByUserIdAndProvider(String userId, String provider) {
        return jpaRepository.existsByUserIdAndProvider(userId, provider);
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteByUserId(String userId) {
        jpaRepository.deleteByUserId(userId);
    }

    @Override
    @Transactional
    public void deleteByUserIdAndProvider(String userId, String provider) {
        jpaRepository.deleteByUserIdAndProvider(userId, provider);
    }

    @Override
    public List<UserProvider> findAll() {
        return jpaRepository.findAll().stream()
                .map(UserProviderMySQLEntity::toDomain)
                .toList();
    }
}
