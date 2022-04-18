package it.alnao.examples;

import java.util.List;
//import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;


public interface ExampleMicro3postgresRepository extends CrudRepository<ExampleMicro3postgresEntity,Long>{
	//List<ExampleMicro3postgresEntity> findAll();
}
