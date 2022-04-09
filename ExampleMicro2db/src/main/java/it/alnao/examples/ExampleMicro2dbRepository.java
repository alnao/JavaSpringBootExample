package it.alnao.examples;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExampleMicro2dbRepository extends JpaRepository<ExampleMicro2dbEntity,Long>{
	List<ExampleMicro2dbEntity> findAll();
}
