package it.alnao.springbootexample.azure.repository;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import it.alnao.springbootexample.azure.entity.AnnotazioneStoricoStatiCosmosEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnotazioneStoricoStatiCosmosRepository extends CosmosRepository<AnnotazioneStoricoStatiCosmosEntity, String> {
    List<AnnotazioneStoricoStatiCosmosEntity> findByIdAnnotazioneOrderByDataCambioDesc(String idAnnotazione);
}
