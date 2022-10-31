package it.alnao.examples;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;


@Service
@CacheConfig(cacheNames= {"ExampleMicro5dynamoService"} )
public class ExampleMicro5dynamoService {
	@Autowired
	private ExampleMicro5dynamoRepository repository;
	  
	@Cacheable
	public Iterable<ExampleMicro5dynamoEntity> findAll(){
		return repository.findAll();
	}
	@Cacheable(value="ExampleMicro5dynamoServicefindById", key="#id", sync=true) //per il singolo elemento
	public Optional<ExampleMicro5dynamoEntity> findById(String id){
		return repository.findById(id);
	}

	@Caching(evict = {
			@CacheEvict(cacheNames="ExampleMicro5dynamoService", allEntries=true),
			@CacheEvict(cacheNames="ExampleMicro5dynamoServicefindById", key="#el.id"),
		})
	public ExampleMicro5dynamoEntity save(ExampleMicro5dynamoEntity el){
		return repository.save(el);
	}

	public void delete(ExampleMicro5dynamoEntity el){
		repository.delete(el);
	}
	
	@Autowired
	CacheManager cacheManager;
	public void clearCache() {
		cacheManager.getCache("ExampleMicro5dynamoService").clear();
		cacheManager.getCache("ExampleMicro5dynamoServicefindById").clear();
	}
}

