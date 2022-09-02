package it.alnao.examples;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ExampleMicro7basicAuthRepository extends MongoRepository<ExampleMicro7basicAuthEntity,String>{
	public ExampleMicro7basicAuthEntity findByUserId(String userId);
}
