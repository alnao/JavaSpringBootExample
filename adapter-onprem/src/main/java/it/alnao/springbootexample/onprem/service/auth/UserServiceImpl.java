package it.alnao.springbootexample.onprem.service.auth;

import it.alnao.springbootexample.core.domain.auth.User;
import it.alnao.springbootexample.core.domain.auth.AccountType;
import it.alnao.springbootexample.core.domain.auth.UserProvider;
import it.alnao.springbootexample.core.repository.auth.UserRepository;
import it.alnao.springbootexample.core.repository.auth.UserProviderRepository;
import it.alnao.springbootexample.core.service.auth.UserService;
import it.alnao.springbootexample.core.service.auth.UserStatistics;

import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementazione OnPrem del service utenti.
 */
@Service
@Profile("onprem")
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final UserProviderRepository userProviderRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, 
                          UserProviderRepository userProviderRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userProviderRepository = userProviderRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    @Transactional
    public User createLocalUser(String username, String email, String password) {
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setAccountType(AccountType.LOCAL);
        user.setEnabled(true);
        user.setEmailVerified(false);
        user.setCreatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    @Override
    @Transactional
    public User createOAuth2User(String email, String firstName, String lastName, 
                                AccountType accountType, String externalId) {
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setAccountType(accountType);
        user.setExternalId(externalId);
        user.setEnabled(true);
        user.setEmailVerified(true); // OAuth2 users have verified emails
        user.setCreatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    @Override
    @Transactional
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
        AccountType accountType = AccountType.fromString(provider);
        return userRepository.findByEmailAndAccountType(email, accountType);
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
    @Transactional
    public void updateLastLogin(String userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        }
    }
    
    @Override
    @Transactional
    public void linkOAuth2Provider(User user, String provider, String externalId, String providerEmail) {
        UserProvider userProvider = new UserProvider();
        userProvider.setId(UUID.randomUUID().toString());
        userProvider.setUserId(user.getId());
        userProvider.setProvider(provider.toUpperCase());
        userProvider.setProviderUserId(externalId);
        userProvider.setProviderEmail(providerEmail);
        userProvider.setCreatedAt(LocalDateTime.now());
        userProvider.setLastUsed(LocalDateTime.now());
        
        userProviderRepository.save(userProvider);
    }
    
    @Override
    @Transactional
    public void unlinkOAuth2Provider(User user, String provider) {
        userProviderRepository.deleteByUserIdAndProvider(user.getId(), provider.toUpperCase());
    }
    
    @Override
    @Transactional
    public void changePassword(String userId, String newPassword) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.isLocalAccount()) {
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
            }
        }
    }
    
    @Override
    @Transactional
    public void setUserEnabled(String userId, boolean enabled) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setEnabled(enabled);
            userRepository.save(user);
        }
    }
    
    @Override
    @Transactional
    public void verifyEmail(String userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setEmailVerified(true);
            userRepository.save(user);
        }
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
        long totalUsers = userRepository.count();
        long localUsers = userRepository.countByAccountType(AccountType.LOCAL);
        long oauth2Users = totalUsers - localUsers;
        
        // For more detailed stats, we'd need additional queries
        // Simplified implementation for now
        long enabledUsers = userRepository.findByEnabled(true).size();
        long disabledUsers = totalUsers - enabledUsers;
        
        return new UserStatistics(totalUsers, localUsers, oauth2Users, 
                                enabledUsers, disabledUsers, 0, 0);
    }
    
    @Override
    @Transactional
    public void deleteUser(String userId) {
        // First delete all providers
        userProviderRepository.deleteByUserId(userId);
        // Then delete the user
        userRepository.deleteById(userId);
    }
}
