package it.alnao.esempio04.service;

import it.alnao.esempio04.model.User;
import it.alnao.esempio04.repository.UserDynamoRepository;

import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserDynamoService {

    @Autowired
    private UserDynamoRepository userDynamoRepository;
    /*
    @Value("${app.database.type:dynamo}")
    private String databaseType;

    public UserDynamoService(AwsServiceClientProvider awsServiceClientProvider) {
        this.userDynamoRepository = new UserDynamoRepository(awsServiceClientProvider.getDynamoDbClient());
    }
         */

    public Object createUser(String name, String email) {
        User user = new User(UUID.randomUUID().toString(), name, email);
        return userDynamoRepository.save(user);
    }

    public Optional<?> getUserById(String id) {
        return userDynamoRepository.findById(id).map(user -> (Object) user);
    }

    public List<?> getAllUsers() {
        return userDynamoRepository.findAll();
    }

    public void deleteUser(String id) {
        userDynamoRepository.deleteById(id);
    }
}