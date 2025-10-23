package it.alnao.springbootexample.azure.repository.auth;

import it.alnao.springbootexample.azure.entity.auth.UserSqlServerEntity;
import it.alnao.springbootexample.core.domain.auth.User;
import it.alnao.springbootexample.core.repository.auth.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@Primary
@Profile("azure")
public class UserSqlServerRepositoryImpl implements it.alnao.springbootexample.core.repository.auth.UserRepository {
    @Autowired
    private UserSqlServerJpaRepository userJpaRepository;

    @Override
    public List<User> findByEnabled(boolean enabled) {
        return userJpaRepository.findAll().stream()
                .map(UserSqlServerEntity::toDomain)
                .filter(user -> user.isEnabled() == enabled)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(String id) {
        userJpaRepository.deleteById(id);
    }

    @Override
    public void delete(User user) {
        userJpaRepository.deleteById(user.getId());
    }

    @Override
    public long count() {
        return userJpaRepository.count();
    }

    @Override
    public List<User> findByAccountType(it.alnao.springbootexample.core.domain.auth.AccountType accountType) {
        return userJpaRepository.findAll().stream()
                .map(UserSqlServerEntity::toDomain)
                .filter(user -> accountType.equals(user.getAccountType()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByUsername(String username) {
        return userJpaRepository.findAll().stream()
                .map(UserSqlServerEntity::toDomain)
                .anyMatch(user -> username.equals(user.getUsername()));
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.findAll().stream()
                .map(UserSqlServerEntity::toDomain)
                .anyMatch(user -> email.equals(user.getEmail()));
    }

    @Override
    public Optional<User> findByExternalIdAndAccountType(String externalId, it.alnao.springbootexample.core.domain.auth.AccountType accountType) {
        return userJpaRepository.findAll().stream()
                .map(UserSqlServerEntity::toDomain)
                .filter(user -> externalId.equals(user.getExternalId()) && accountType.equals(user.getAccountType()))
                .findFirst();
    }

    @Override
    public Optional<User> findByEmailAndAccountType(String email, it.alnao.springbootexample.core.domain.auth.AccountType accountType) {
        return userJpaRepository.findAll().stream()
                .map(UserSqlServerEntity::toDomain)
                .filter(user -> email.equals(user.getEmail()) && accountType.equals(user.getAccountType()))
                .findFirst();
    }

    @Override
    public long countByAccountType(it.alnao.springbootexample.core.domain.auth.AccountType accountType) {
        return userJpaRepository.findAll().stream()
                .map(UserSqlServerEntity::toDomain)
                .filter(user -> accountType.equals(user.getAccountType()))
                .count();
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(UUID.randomUUID().toString());
        }
        user.setUpdatedAt(LocalDateTime.now());
        UserSqlServerEntity entity = UserSqlServerEntity.fromDomain(user);
        UserSqlServerEntity savedEntity = userJpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<User> findById(String id) {
        return userJpaRepository.findById(id)
                .map(UserSqlServerEntity::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userJpaRepository.findByUsername(username)
                .map(UserSqlServerEntity::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email)
                .map(UserSqlServerEntity::toDomain);
    }

    @Override
    public List<User> findAll() {
        return userJpaRepository.findAll().stream()
                .map(UserSqlServerEntity::toDomain)
                .collect(Collectors.toList());
    }
}
