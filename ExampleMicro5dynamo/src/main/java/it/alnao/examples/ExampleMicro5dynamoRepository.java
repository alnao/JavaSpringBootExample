package it.alnao.examples;

//import java.util.Optional;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.socialsignin.spring.data.dynamodb.repository.EnableScanCount;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@EnableScan
@Repository
public interface ExampleMicro5dynamoRepository extends CrudRepository<ExampleMicro5dynamoEntity, String> {
	
    @EnableScanCount
    long countByNome(String nome);
    
	//@EnableScan
	//@Override
	//Iterable<ExampleMicro5dynamoEntity> findAll();
	
    //Optional<ExampleMicro5dynamoEntity> findById(String id);
}
