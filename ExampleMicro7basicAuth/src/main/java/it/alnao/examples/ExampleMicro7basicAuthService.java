package it.alnao.examples;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
@Transactional(readOnly = true)
public class ExampleMicro7basicAuthService {
	@Autowired
	ExampleMicro7basicAuthRepository repository;
	
	public List<ExampleMicro7basicAuthEntity> findAll(){
		return repository.findAll();
	}

	public ExampleMicro7basicAuthEntity findByUserId(String nome){
		return repository.findByUserId(nome);
	}

	public ExampleMicro7basicAuthEntity save(ExampleMicro7basicAuthEntity el){
		return repository.save(el);
	}

	public void delete(ExampleMicro7basicAuthEntity el){
		repository.delete(el);
	}
}