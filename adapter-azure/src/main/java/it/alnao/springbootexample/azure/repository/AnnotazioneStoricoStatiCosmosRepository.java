package it.alnao.springbootexample.azure.repository;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import it.alnao.springbootexample.azure.entity.AnnotazioneStoricoStatiCosmosEntity;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
//import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Primary
@Profile("azure")
//extends CosmosRepository
public interface AnnotazioneStoricoStatiCosmosRepository extends CosmosRepository<AnnotazioneStoricoStatiCosmosEntity, String> {
    List<AnnotazioneStoricoStatiCosmosEntity> findByIdAnnotazioneOrderByDataCambioDesc(String idAnnotazione);
}
