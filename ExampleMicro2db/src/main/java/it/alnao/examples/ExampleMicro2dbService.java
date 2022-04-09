package it.alnao.examples;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExampleMicro2dbService {

	@Autowired
	ExampleMicro2dbRepository repo;
	public List<ExampleMicro2dbEntity> getAll() {
	    return repo.findAll();
	}
}
