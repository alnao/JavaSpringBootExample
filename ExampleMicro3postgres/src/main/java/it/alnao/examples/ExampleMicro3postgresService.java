package it.alnao.examples;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExampleMicro3postgresService {

	@Autowired
	ExampleMicro3postgresRepository repo;
	
	public Iterable<ExampleMicro3postgresEntity> getAll() {
	    return repo.findAll();
	}
	
	public ExampleMicro3postgresEntity save(ExampleMicro3postgresEntity el) {
		return repo.save(el);
	}
	
}
