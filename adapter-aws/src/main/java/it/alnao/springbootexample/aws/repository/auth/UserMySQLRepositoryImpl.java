package it.alnao.springbootexample.aws.repository.auth;

import it.alnao.springbootexample.aws.entity.auth.UserMySQLEntity;
import it.alnao.springbootexample.port.domain.auth.AccountType;
import it.alnao.springbootexample.port.domain.auth.User;
import it.alnao.springbootexample.port.repository.auth.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementazione MySQL del repository per gli utenti AWS.
 */
@Repository
@Primary
@Profile("aws")
public class UserMySQLRepositoryImpl implements UserRepository {

    private final UserMySQLJpaRepository userJpaRepository;

    @Autowired
    public UserMySQLRepositoryImpl(UserMySQLJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(UUID.randomUUID().toString());
        }
        user.setUpdatedAt(LocalDateTime.now());
        
        UserMySQLEntity entity = UserMySQLEntity.fromDomain(user);
        UserMySQLEntity savedEntity = userJpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<User> findById(String id) {
        return userJpaRepository.findById(id)
                .map(UserMySQLEntity::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userJpaRepository.findByUsername(username)
                .map(UserMySQLEntity::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email)
                .map(UserMySQLEntity::toDomain);
    }

    @Override
    public Optional<User> findByEmailAndAccountType(String email, AccountType accountType) {
        return userJpaRepository.findByEmailAndAccountType(email, accountType.name())
                .map(UserMySQLEntity::toDomain);
    }

    @Override
    public Optional<User> findByExternalIdAndAccountType(String externalId, AccountType accountType) {
        return userJpaRepository.findByExternalIdAndAccountType(externalId, accountType.name())
                .map(UserMySQLEntity::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userJpaRepository.existsByUsername(username);
    }

    @Override
    public List<User> findAll() {
        return userJpaRepository.findAll()
                .stream()
                .map(UserMySQLEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findByAccountType(AccountType accountType) {
        return userJpaRepository.findByAccountType(accountType.name())
                .stream()
                .map(UserMySQLEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findByEnabled(boolean enabled) {
        return userJpaRepository.findByEnabled(enabled)
                .stream()
                .map(UserMySQLEntity::toDomain)
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
    public long countByAccountType(AccountType accountType) {
        return userJpaRepository.countByAccountType(accountType.name());
    }
}
