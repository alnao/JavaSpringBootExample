package it.alnao.examples;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

@Service
@CacheConfig(cacheNames= {"ExampleMicro6cacheService"} )
public class ExampleMicro6cacheService {

	@Autowired
	ExampleMicro6cacheRepository repo;
	
	@Autowired
	CacheManager cacheManager;
	public void clearCache() {
		cacheManager.getCache("ExampleMicro5dynamoService").clear();
		cacheManager.getCache("ExampleMicro5dynamoServicefindById").clear();
	}
	
	public ExampleMicro6cacheEntity save(ExampleMicro6cacheEntity el){
		return repo.save(el);
	}
	
	@Cacheable("list")
	public List<ExampleMicro6cacheEntity> getAll() {
		try {
			TimeUnit.SECONDS.sleep(5);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return repo.findAll();
	}
}
