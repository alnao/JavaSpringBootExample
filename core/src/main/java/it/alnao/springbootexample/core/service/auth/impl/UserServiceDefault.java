package it.alnao.springbootexample.core.service.auth.impl;

import it.alnao.springbootexample.core.domain.auth.AccountType;
import it.alnao.springbootexample.core.domain.auth.User;
import it.alnao.springbootexample.core.repository.auth.UserRepository;
import it.alnao.springbootexample.core.service.auth.UserService;
import it.alnao.springbootexample.core.service.auth.UserStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Default, profile-agnostic implementation of UserService living in core.
 */
@Service
@Primary
public class UserServiceDefault implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceDefault(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User createLocalUser(String username, String email, String password) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setAccountType(AccountType.LOCAL);
        return userRepository.save(user);
    }

    @Override
    public User createOAuth2User(String email, String firstName, String lastName, AccountType accountType, String externalId) {
        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setAccountType(accountType);
        user.setExternalId(externalId);
        return userRepository.save(user);
    }

    @Override
    public User updateOAuth2User(User user, String firstName, String lastName, String avatarUrl) {
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setAvatarUrl(avatarUrl);
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findById(String userId) {
        return userRepository.findById(userId);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findByEmailAndProvider(String email, String provider) {
        // provider not persisted directly in core; approximate via account type LOCAL for typical flows
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findByExternalIdAndAccountType(String externalId, AccountType accountType) {
        return userRepository.findByExternalIdAndAccountType(externalId, accountType);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public void updateLastLogin(String userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setLastLogin(java.time.LocalDateTime.now());
            userRepository.save(user);
        });
    }

    @Override
    public void linkOAuth2Provider(User user, String provider, String externalId, String providerEmail) {
        // Minimal core linkage: update externalId and email if provided
        if (externalId != null) user.setExternalId(externalId);
        if (providerEmail != null) user.setEmail(providerEmail);
        userRepository.save(user);
    }

    @Override
    public void unlinkOAuth2Provider(User user, String provider) {
        // Minimal unlink: clear externalId (provider not tracked explicitly)
        user.setExternalId(null);
        userRepository.save(user);
    }

    @Override
    public void changePassword(String userId, String newPassword) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setPassword(newPassword);
            userRepository.save(user);
        });
    }

    @Override
    public void setUserEnabled(String userId, boolean enabled) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setEnabled(enabled);
            userRepository.save(user);
        });
    }

    @Override
    public void verifyEmail(String userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setEmailVerified(true);
            userRepository.save(user);
        });
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public List<User> findUsersByAccountType(AccountType accountType) {
        return userRepository.findByAccountType(accountType);
    }

    @Override
    public UserStatistics getUserStatistics() {
        UserStatistics stats = new UserStatistics();
        stats.setTotalUsers(userRepository.count());
        stats.setLocalUsers(userRepository.countByAccountType(AccountType.LOCAL));
        stats.setEnabledUsers(userRepository.findByEnabled(true).size());
        return stats;
    }

    @Override
    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }
}
