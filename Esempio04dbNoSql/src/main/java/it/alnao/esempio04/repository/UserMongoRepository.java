package it.alnao.esempio04.repository;

import it.alnao.esempio04.model.UserMongo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMongoRepository extends MongoRepository<UserMongo, String> {
    
}