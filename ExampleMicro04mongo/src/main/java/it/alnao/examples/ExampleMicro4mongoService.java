package it.alnao.examples;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ExampleMicro4mongoService {
	@Autowired
	ExampleMicro4mongoReposiroty repository;
	
	public List<ExampleMicro4mongoEntity> findAll(){
		return repository.findAll();
	}

	public ExampleMicro4mongoEntity findByNome(String nome){
		return repository.findByNome(nome);
	}

	public ExampleMicro4mongoEntity save(ExampleMicro4mongoEntity el){
		return repository.save(el);
	}

	public void delete(ExampleMicro4mongoEntity el){
		repository.delete(el);
	}
}