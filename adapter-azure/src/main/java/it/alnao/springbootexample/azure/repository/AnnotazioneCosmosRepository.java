package it.alnao.springbootexample.azure.repository;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import it.alnao.springbootexample.azure.entity.AnnotazioneCosmosEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnotazioneCosmosRepository extends CosmosRepository<AnnotazioneCosmosEntity, String> {
    List<AnnotazioneCosmosEntity> findByValoreNotaContainingIgnoreCase(String valoreNota);
}
