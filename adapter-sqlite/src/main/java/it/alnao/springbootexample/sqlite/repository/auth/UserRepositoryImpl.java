package it.alnao.springbootexample.sqlite.repository.auth;

import it.alnao.springbootexample.core.domain.auth.User;
import it.alnao.springbootexample.core.domain.auth.AccountType;
import it.alnao.springbootexample.core.repository.auth.UserRepository;
import it.alnao.springbootexample.sqlite.entity.auth.UserSQLiteEntity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Profile("sqlite")
public class UserRepositoryImpl implements UserRepository {
    @Autowired
    private UserSQLiteRepository userSQLiteRepository;
    
    @Override
    public Optional<User> findByExternalIdAndAccountType(String externalId, AccountType accountType) {
        return userSQLiteRepository.findAll().stream()
            .filter(u -> externalId.equals(u.getExternalId()) && u.getAccountType() == accountType)
            .findFirst()
            .map(UserSQLiteEntity::toDomain);
    }
    @Override
    public boolean existsByEmail(String email) {
        return userSQLiteRepository.findByEmail(email).isPresent();
    }
    @Override
    public List<User> findByAccountType(AccountType accountType) {
        return userSQLiteRepository.findAll().stream()
            .filter(u -> u.getAccountType() == accountType)
            .map(UserSQLiteEntity::toDomain)
            .collect(Collectors.toList());
    }


    @Override
    public User save(User user) {
        UserSQLiteEntity entity = UserSQLiteEntity.fromDomain(user);
        UserSQLiteEntity savedEntity = userSQLiteRepository.save(entity);
        return savedEntity.toDomain();
    }
    @Override
    public Optional<User> findByEmailAndAccountType(String email, AccountType accountType) {
        return userSQLiteRepository.findAll().stream()
            .filter(u -> email.equals(u.getEmail()) && u.getAccountType() == accountType)
            .findFirst()
            .map(UserSQLiteEntity::toDomain);
    }
    @Override
    public Optional<User> findById(String id) {
        return userSQLiteRepository.findById(id).map(UserSQLiteEntity::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userSQLiteRepository.findByUsername(username).map(UserSQLiteEntity::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userSQLiteRepository.findByEmail(email).map(UserSQLiteEntity::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userSQLiteRepository.findByUsername(username).isPresent();
    }

    @Override
    public List<User> findAll() {
        return userSQLiteRepository.findAll().stream().map(UserSQLiteEntity::toDomain).collect(Collectors.toList());
    }

    // ...existing code...

    @Override
    public List<User> findByEnabled(boolean enabled) {
        return userSQLiteRepository.findAll().stream()
            .filter(u -> u.isEnabled() == enabled)
            .map(UserSQLiteEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public void deleteById(String id) {
        userSQLiteRepository.deleteById(id);
    }

    @Override
    public void delete(User user) {
        userSQLiteRepository.deleteById(user.getId());
    }

    @Override
    public long count() {
        return userSQLiteRepository.count();
    }

    @Override
    public long countByAccountType(AccountType accountType) {
        return userSQLiteRepository.findAll().stream()
            .filter(u -> u.getAccountType() == accountType)
            .count();
    }
}
