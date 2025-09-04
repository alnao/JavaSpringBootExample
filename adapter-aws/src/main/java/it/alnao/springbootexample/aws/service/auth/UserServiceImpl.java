package it.alnao.springbootexample.aws.service.auth;

import it.alnao.springbootexample.port.domain.auth.AccountType;
import it.alnao.springbootexample.port.domain.auth.User;
import it.alnao.springbootexample.port.domain.auth.UserRole;
import it.alnao.springbootexample.port.repository.auth.UserProviderRepository;
import it.alnao.springbootexample.port.repository.auth.UserRepository;
import it.alnao.springbootexample.port.service.auth.UserService;
import it.alnao.springbootexample.port.service.auth.UserStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementazione AWS del service per la gestione degli utenti.
 */
@Service
@Profile("aws")
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserProviderRepository userProviderRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
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
        user.setRole(UserRole.USER);
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
        user.setRole(UserRole.USER);
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
        user.setUpdatedAt(LocalDateTime.now());
        
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
        // Per implementazione AWS, cerchiamo prima per email poi filtriamo per provider
        return userRepository.findByEmail(email)
                .filter(user -> user.hasProvider(provider));
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
        userRepository.findById(userId).ifPresent(user -> {
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        });
    }
    
    @Override
    @Transactional
    public void linkOAuth2Provider(User user, String provider, String externalId, String providerEmail) {
        // Implementazione specifica per AWS se necessario
        // Per ora, delega al repository dei provider
    }
    
    @Override
    @Transactional
    public void unlinkOAuth2Provider(User user, String provider) {
        userProviderRepository.deleteByUserIdAndProvider(user.getId(), provider);
    }
    
    @Override
    @Transactional
    public void changePassword(String userId, String newPassword) {
        userRepository.findById(userId).ifPresent(user -> {
            if (user.getAccountType() == AccountType.LOCAL) {
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);
            }
        });
    }
    
    @Override
    @Transactional
    public void setUserEnabled(String userId, boolean enabled) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setEnabled(enabled);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        });
    }
    
    @Override
    @Transactional
    public void verifyEmail(String userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setEmailVerified(true);
            user.setUpdatedAt(LocalDateTime.now());
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
        stats.setOauth2Users(stats.getTotalUsers() - stats.getLocalUsers());
        
        // Calcola altri statistiche se necessario
        List<User> enabledUsers = userRepository.findByEnabled(true);
        stats.setEnabledUsers(enabledUsers.size());
        stats.setDisabledUsers((int) (stats.getTotalUsers() - stats.getEnabledUsers()));
        
        return stats;
    }
    
    @Override
    @Transactional
    public void deleteUser(String userId) {
        // Prima elimina i provider collegati
        userProviderRepository.deleteByUserId(userId);
        // Poi elimina l'utente
        userRepository.deleteById(userId);
    }
}
