package it.alnao.springbootexample.azure.repository;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import it.alnao.springbootexample.azure.entity.AnnotazioneCosmosEntity;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
//import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Profile("azure")
public interface AnnotazioneCosmosRepository extends CosmosRepository<AnnotazioneCosmosEntity, String> {
    
    List<AnnotazioneCosmosEntity> findByValoreNotaContainingIgnoreCase(String valoreNota);
    List<AnnotazioneCosmosEntity> findByVersioneNota(String versioneNota);
    //List<AnnotazioneCosmosEntity> findByUtente(String utente);
    //List<AnnotazioneCosmosEntity> findByStato(String stato);
    //List<AnnotazioneCosmosEntity> findByUtenteAndStato(String utente, String stato);
    //long countByStato(String stato);
    //long countByUtente(String utente);
}