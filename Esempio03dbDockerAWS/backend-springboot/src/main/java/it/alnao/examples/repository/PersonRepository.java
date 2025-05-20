package it.alnao.examples.repository;
import it.alnao.examples.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PersonRepository extends JpaRepository<Person, Long> {}
