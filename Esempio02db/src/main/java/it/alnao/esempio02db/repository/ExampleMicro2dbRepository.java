package it.alnao.esempio02db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;

import it.alnao.esempio02db.entity.ExampleMicro2dbEntity;

//@Repository
public interface ExampleMicro2dbRepository extends JpaRepository<ExampleMicro2dbEntity, Long> {
    
}
