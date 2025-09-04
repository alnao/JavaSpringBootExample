package it.alnao.springbootexample.aws.repository.auth;

import it.alnao.springbootexample.aws.entity.auth.UserDynamoEntity;
import it.alnao.springbootexample.port.domain.auth.AccountType;
import it.alnao.springbootexample.port.domain.auth.User;
import it.alnao.springbootexample.port.repository.auth.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementazione DynamoDB del repository per gli utenti.
 */
@Repository
@Profile("aws")
public class UserDynamoRepositoryImpl implements UserRepository {

    private final DynamoDbTable<UserDynamoEntity> userTable;

    @Autowired
    public UserDynamoRepositoryImpl(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.userTable = dynamoDbEnhancedClient.table("users", TableSchema.fromBean(UserDynamoEntity.class));
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(UUID.randomUUID().toString());
        }
        user.setUpdatedAt(LocalDateTime.now());
        
        UserDynamoEntity entity = UserDynamoEntity.fromDomain(user);
        userTable.putItem(entity);
        return entity.toDomain();
    }

    @Override
    public Optional<User> findById(String id) {
        UserDynamoEntity entity = userTable.getItem(Key.builder().partitionValue(id).build());
        return entity != null ? Optional.of(entity.toDomain()) : Optional.empty();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        QueryConditional queryConditional = QueryConditional.keyEqualTo(Key.builder().partitionValue(username).build());
        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .build();
        
        return userTable.index("username-index")
                .query(request)
                .stream()
                .flatMap(page -> page.items().stream())
                .findFirst()
                .map(UserDynamoEntity::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        QueryConditional queryConditional = QueryConditional.keyEqualTo(Key.builder().partitionValue(email).build());
        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .build();
        
        return userTable.index("email-index")
                .query(request)
                .stream()
                .flatMap(page -> page.items().stream())
                .findFirst()
                .map(UserDynamoEntity::toDomain);
    }

    @Override
    public Optional<User> findByEmailAndAccountType(String email, AccountType accountType) {
        return findByEmail(email)
                .filter(user -> user.getAccountType() == accountType);
    }

    @Override
    public Optional<User> findByExternalIdAndAccountType(String externalId, AccountType accountType) {
        QueryConditional queryConditional = QueryConditional.keyEqualTo(Key.builder().partitionValue(externalId).build());
        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .build();
        
        return userTable.index("external-id-index")
                .query(request)
                .stream()
                .flatMap(page -> page.items().stream())
                .map(UserDynamoEntity::toDomain)
                .filter(user -> user.getAccountType() == accountType)
                .findFirst();
    }

    @Override
    public List<User> findByEnabled(boolean enabled) {
        return userTable.scan()
                .stream()
                .flatMap(page -> page.items().stream())
                .map(UserDynamoEntity::toDomain)
                .filter(user -> user.isEnabled() == enabled)
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return userTable.scan()
                .stream()
                .flatMap(page -> page.items().stream())
                .count();
    }

    @Override
    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    @Override
    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }

    @Override
    public List<User> findByAccountType(AccountType accountType) {
        // Per DynamoDB, dovremo fare una scan - non ottimale per grandi dataset
        return userTable.scan()
                .stream()
                .flatMap(page -> page.items().stream())
                .map(UserDynamoEntity::toDomain)
                .filter(user -> user.getAccountType() == accountType)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findAll() {
        return userTable.scan()
                .stream()
                .flatMap(page -> page.items().stream())
                .map(UserDynamoEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countByAccountType(AccountType accountType) {
        return findByAccountType(accountType).size();
    }

    @Override
    public void deleteById(String id) {
        userTable.deleteItem(Key.builder().partitionValue(id).build());
    }

    @Override
    public void delete(User user) {
        deleteById(user.getId());
    }
}
