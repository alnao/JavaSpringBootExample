package it.alnao.examples;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExampleMicro6cacheRepository extends JpaRepository<ExampleMicro6cacheEntity,Long>{
	List<ExampleMicro6cacheEntity> findAll();
}
