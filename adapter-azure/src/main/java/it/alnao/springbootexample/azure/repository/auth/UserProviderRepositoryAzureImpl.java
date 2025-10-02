package it.alnao.springbootexample.azure.repository.auth;

import it.alnao.springbootexample.azure.entity.auth.UserProviderSqlServerEntity;
import it.alnao.springbootexample.core.domain.auth.UserProvider;
import it.alnao.springbootexample.core.repository.auth.UserProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@Profile("azure")
public class UserProviderRepositoryAzureImpl implements UserProviderRepository {
    @Override
    public boolean existsByUserIdAndProvider(String userId, String provider) {
        return jpaRepository.findAll().stream()
                .anyMatch(e -> userId.equals(e.getUserId()) && provider.equals(e.getProvider()));
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteByUserId(String userId) {
        jpaRepository.findAll().stream()
            .filter(e -> userId.equals(e.getUserId()))
            .forEach(e -> jpaRepository.deleteById(e.getId()));
    }

    @Override
    @Transactional
    public void deleteByUserIdAndProvider(String userId, String provider) {
        jpaRepository.findAll().stream()
            .filter(e -> userId.equals(e.getUserId()) && provider.equals(e.getProvider()))
            .forEach(e -> jpaRepository.deleteById(e.getId()));
    }
    @Autowired
    private UserProviderSqlServerRepository jpaRepository;

    @Override
    public List<UserProvider> findAll() {
        return jpaRepository.findAll().stream()
                .map(UserProviderSqlServerEntity::toDomain)
                .toList();
    }

    @Override
    public UserProvider save(UserProvider userProvider) {
        if (userProvider.getId() == null) {
            userProvider.setId(UUID.randomUUID().toString());
        }
        UserProviderSqlServerEntity entity = UserProviderSqlServerEntity.fromDomain(userProvider);
        UserProviderSqlServerEntity savedEntity = jpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<UserProvider> findById(String id) {
        return jpaRepository.findById(id)
                .map(UserProviderSqlServerEntity::toDomain);
    }

    @Override
    public List<UserProvider> findByUserId(String userId) {
        return jpaRepository.findByUserId(userId).stream()
                .map(UserProviderSqlServerEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<UserProvider> findByUserIdAndProvider(String userId, String provider) {
        return jpaRepository.findAll().stream()
                .filter(e -> userId.equals(e.getUserId()) && provider.equals(e.getProvider()))
                .findFirst()
                .map(UserProviderSqlServerEntity::toDomain);
    }

    @Override
    public Optional<UserProvider> findByProviderAndProviderUserId(String provider, String providerUserId) {
        return jpaRepository.findByProviderAndProviderUserId(provider, providerUserId)
                .map(UserProviderSqlServerEntity::toDomain);
    }
}
