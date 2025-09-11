package it.alnao.springbootexample.sqlite.service.auth;

import it.alnao.springbootexample.core.service.auth.UserService;
import it.alnao.springbootexample.core.domain.auth.User;
import it.alnao.springbootexample.core.domain.auth.AccountType;
import it.alnao.springbootexample.core.service.auth.UserStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Profile("sqlite")
public class UserSQLiteAdapterServiceImpl implements UserService {
    @Autowired
    private UserSQLiteAdapterService userRepository;

    @Override
    public User createLocalUser(String username, String email, String password) {
        // Implementazione semplificata
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
        // Implementazione semplificata
        return userRepository.findByEmail(email); // Ignora provider
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
        return userRepository.findByUsername(username).isPresent();
    }

    @Override
    public void updateLastLogin(String userId) {
        // No-op per SQLite
    }

    @Override
    public void linkOAuth2Provider(User user, String provider, String externalId, String providerEmail) {
        // No-op per SQLite
    }

    @Override
    public void unlinkOAuth2Provider(User user, String provider) {
        // No-op per SQLite
    }

    @Override
    public void changePassword(String userId, String newPassword) {
        Optional<User> userOpt = userRepository.findById(userId);
        userOpt.ifPresent(user -> {
            user.setPassword(newPassword);
            userRepository.save(user);
        });
    }

    @Override
    public void setUserEnabled(String userId, boolean enabled) {
        Optional<User> userOpt = userRepository.findById(userId);
        userOpt.ifPresent(user -> {
            user.setEnabled(enabled);
            userRepository.save(user);
        });
    }

    @Override
    public void verifyEmail(String userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        userOpt.ifPresent(user -> {
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
        // Implementazione semplificata
        return new UserStatistics();
    }

    @Override
    public void deleteUser(String userId) {
        // Implementazione semplificata
    }
}
