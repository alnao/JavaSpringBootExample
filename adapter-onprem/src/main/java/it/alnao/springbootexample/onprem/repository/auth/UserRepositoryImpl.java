package it.alnao.springbootexample.onprem.repository.auth;

import it.alnao.springbootexample.onprem.entity.auth.UserEntity;
import it.alnao.springbootexample.core.domain.auth.User;
import it.alnao.springbootexample.core.domain.auth.AccountType;
import it.alnao.springbootexample.core.repository.auth.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementazione PostgreSQL del repository utenti.
 */
@Repository
@Profile("onprem")
public class UserRepositoryImpl implements UserRepository {
    
    @Autowired
    private UserJpaRepository jpaRepository;
    
    @Override
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(UUID.randomUUID().toString());
        }
        
        UserEntity entity = UserEntity.fromDomain(user);
        UserEntity savedEntity = jpaRepository.save(entity);
        return savedEntity.toDomain();
    }
    
    @Override
    public Optional<User> findById(String id) {
        return jpaRepository.findByIdWithProviders(id)
                .map(UserEntity::toDomain);
    }
    
    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username)
                .map(UserEntity::toDomain);
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email)
                .map(UserEntity::toDomain);
    }
    
    @Override
    public Optional<User> findByEmailAndAccountType(String email, AccountType accountType) {
        return jpaRepository.findByEmailAndAccountType(email, accountType)
                .map(UserEntity::toDomain);
    }
    
    @Override
    public Optional<User> findByExternalIdAndAccountType(String externalId, AccountType accountType) {
        return jpaRepository.findByExternalIdAndAccountType(externalId, accountType)
                .map(UserEntity::toDomain);
    }
    
    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }
    
    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }
    
    @Override
    public List<User> findAll() {
        return jpaRepository.findAll().stream()
                .map(UserEntity::toDomain)
                .toList();
    }
    
    @Override
    public List<User> findByAccountType(AccountType accountType) {
        return jpaRepository.findByAccountType(accountType).stream()
                .map(UserEntity::toDomain)
                .toList();
    }
    
    @Override
    public List<User> findByEnabled(boolean enabled) {
        return jpaRepository.findByEnabled(enabled).stream()
                .map(UserEntity::toDomain)
                .toList();
    }
    
    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }
    
    @Override
    public void delete(User user) {
        jpaRepository.deleteById(user.getId());
    }
    
    @Override
    public long count() {
        return jpaRepository.count();
    }
    
    @Override
    public long countByAccountType(AccountType accountType) {
        return jpaRepository.countByAccountType(accountType);
    }
}
