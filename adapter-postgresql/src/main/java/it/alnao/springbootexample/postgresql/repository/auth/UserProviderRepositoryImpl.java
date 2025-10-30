package it.alnao.springbootexample.postgresql.repository.auth;

import it.alnao.springbootexample.postgresql.entity.auth.UserProviderEntity;
import it.alnao.springbootexample.core.domain.auth.UserProvider;
import it.alnao.springbootexample.core.repository.auth.UserProviderRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementazione PostgreSQL del repository provider OAuth2.
 */
@Repository
@Profile("kube")
public class UserProviderRepositoryImpl implements UserProviderRepository {
    
    @Autowired
    private UserProviderJpaRepository jpaRepository;
    
    @Override
    public UserProvider save(UserProvider userProvider) {
        if (userProvider.getId() == null) {
            userProvider.setId(UUID.randomUUID().toString());
        }
        
        UserProviderEntity entity = UserProviderEntity.fromDomain(userProvider);
        UserProviderEntity savedEntity = jpaRepository.save(entity);
        return savedEntity.toDomain();
    }
    
    @Override
    public Optional<UserProvider> findById(String id) {
        return jpaRepository.findById(id)
                .map(UserProviderEntity::toDomain);
    }
    
    @Override
    public List<UserProvider> findByUserId(String userId) {
        return jpaRepository.findByUserId(userId).stream()
                .map(UserProviderEntity::toDomain)
                .toList();
    }
    
    @Override
    public Optional<UserProvider> findByUserIdAndProvider(String userId, String provider) {
        return jpaRepository.findByUserIdAndProvider(userId, provider)
                .map(UserProviderEntity::toDomain);
    }
    
    @Override
    public Optional<UserProvider> findByProviderAndProviderUserId(String provider, String providerUserId) {
        return jpaRepository.findByProviderAndProviderUserId(provider, providerUserId)
                .map(UserProviderEntity::toDomain);
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
                .map(UserProviderEntity::toDomain)
                .toList();
    }
}
