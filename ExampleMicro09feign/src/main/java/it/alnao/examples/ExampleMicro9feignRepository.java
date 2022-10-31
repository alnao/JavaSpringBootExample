package it.alnao.examples;



import org.springframework.data.mongodb.repository.MongoRepository;

public interface ExampleMicro9feignRepository extends MongoRepository<ExampleMicro9feignEntity,String>{
    public ExampleMicro9feignEntity findByNome(String nome);
}