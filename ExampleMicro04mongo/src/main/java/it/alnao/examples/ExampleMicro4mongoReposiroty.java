package it.alnao.examples;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ExampleMicro4mongoReposiroty extends MongoRepository<ExampleMicro4mongoEntity,String>{
	public ExampleMicro4mongoEntity findByNome(String nome);
}