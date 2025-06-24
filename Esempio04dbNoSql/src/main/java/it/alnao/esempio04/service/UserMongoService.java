package it.alnao.esempio04.service;

import it.alnao.esempio04.model.UserMongo;
import it.alnao.esempio04.repository.UserMongoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserMongoService {


    @Autowired
    private UserMongoRepository userMongoRepository;
    
    @Value("${app.database.type:dynamo}")
    private String databaseType;



    public Object createUser(String name, String email) {
        UserMongo user = new UserMongo(name, email);
        return userMongoRepository.save(user);
    }

    public Optional<?> getUserById(String id) {
        return userMongoRepository.findById(id).map(user -> (Object) user);
    }

    public List<?> getAllUsers() {
        return userMongoRepository.findAll();
    }

    public void deleteUser(String id) {
        userMongoRepository.deleteById(id);
    }
}