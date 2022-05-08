package it.alnao.examples;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ExampleMicro5dynamoService {
	@Autowired
	private ExampleMicro5dynamoRepository repository;
	  
	public Iterable<ExampleMicro5dynamoEntity> findAll(){
		return repository.findAll();
	}

	public Optional<ExampleMicro5dynamoEntity> findById(String id){
		return repository.findById(id);
	}

	public ExampleMicro5dynamoEntity save(ExampleMicro5dynamoEntity el){
		return repository.save(el);
	}

	public void delete(ExampleMicro5dynamoEntity el){
		repository.delete(el);
	}
}

