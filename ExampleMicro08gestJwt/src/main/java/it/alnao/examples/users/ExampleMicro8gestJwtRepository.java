package it.alnao.examples.users;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ExampleMicro8gestJwtRepository extends MongoRepository<ExampleMicro8gestJwtEntity,String>{
	public List<ExampleMicro8gestJwtEntity> findByUserId(String userId);
}
