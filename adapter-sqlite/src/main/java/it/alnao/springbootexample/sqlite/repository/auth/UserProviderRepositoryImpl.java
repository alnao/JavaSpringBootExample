package it.alnao.springbootexample.sqlite.repository.auth;

import it.alnao.springbootexample.core.domain.auth.UserProvider;
import it.alnao.springbootexample.core.repository.auth.UserProviderRepository;
import it.alnao.springbootexample.sqlite.entity.auth.UserProviderSQLiteEntity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Profile("sqlite")
public class UserProviderRepositoryImpl implements UserProviderRepository {
    @Autowired
    private UserProviderSQLiteRepository userProviderSQLiteRepository;

    @Override
    public UserProvider save(UserProvider userProvider) {
        UserProviderSQLiteEntity entity = UserProviderSQLiteEntity.fromDomain(userProvider);
        UserProviderSQLiteEntity savedEntity = userProviderSQLiteRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<UserProvider> findById(String id) {
        return userProviderSQLiteRepository.findById(id)
                .map(UserProviderSQLiteEntity::toDomain);
    }

    @Override
    public List<UserProvider> findAll() {
        return userProviderSQLiteRepository.findAll().stream()
                .map(UserProviderSQLiteEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<UserProvider> findByUserIdAndProvider(String userId, String provider) {
        // TODO: Add method to repository if needed
        return findByUserId(userId).stream()
                .filter(up -> provider.equals(up.getProvider()))
                .findFirst();
    }

    @Override
    public Optional<UserProvider> findByProviderAndProviderUserId(String provider, String providerUserId) {
        // TODO: Add method to repository if needed
        return findAll().stream()
                .filter(up -> provider.equals(up.getProvider()) && providerUserId.equals(up.getProviderUserId()))
                .findFirst();
    }

    @Override
    public boolean existsByUserIdAndProvider(String userId, String provider) {
        return findByUserIdAndProvider(userId, provider).isPresent();
    }

    @Override
    public void deleteById(String id) {
        userProviderSQLiteRepository.deleteById(id);
    }

    @Override
    public void deleteByUserId(String userId) {
        findByUserId(userId).forEach(up -> deleteById(up.getId()));
    }

    @Override
    public void deleteByUserIdAndProvider(String userId, String provider) {
        findByUserIdAndProvider(userId, provider).ifPresent(up -> deleteById(up.getId()));
    }


    @Override
    public List<UserProvider> findByUserId(String userId) {
        return userProviderSQLiteRepository.findAll().stream()
            .filter(up -> up.getUserId().equals(userId))
            .map(UserProviderSQLiteEntity::toDomain)
            .collect(Collectors.toList());
    }
}
