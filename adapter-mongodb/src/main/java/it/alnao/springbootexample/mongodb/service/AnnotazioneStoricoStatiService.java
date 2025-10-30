package it.alnao.springbootexample.mongodb.service;

import it.alnao.springbootexample.core.domain.AnnotazioneStoricoStati;
import it.alnao.springbootexample.mongodb.entity.AnnotazioneStoricoStatiEntity;
import it.alnao.springbootexample.mongodb.repository.AnnotazioneStoricoStatiMongoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service per gestire lo storico dei cambi di stato delle annotazioni (MongoDB)
 */
@Service
@Profile("kube")
public class AnnotazioneStoricoStatiService implements it.alnao.springbootexample.core.service.AnnotazioneStoricoStatiService {

    @Autowired
    private AnnotazioneStoricoStatiMongoRepository storicoStatiRepository;

    /**
     * Inserisce un nuovo record di storico cambio stato
     */
    @Override
    public AnnotazioneStoricoStati inserisciCambioStato(String idAnnotazione, String versione, 
                                                       String statoNew, String statoOld, 
                                                       String utente, String notaOperazione) {
        String idOperazione = UUID.randomUUID().toString();
        LocalDateTime dataModifica = LocalDateTime.now();
        
        AnnotazioneStoricoStatiEntity entity = new AnnotazioneStoricoStatiEntity(
            idOperazione, idAnnotazione, versione, statoNew, statoOld, 
            utente, dataModifica, notaOperazione
        );
        
        AnnotazioneStoricoStatiEntity savedEntity = storicoStatiRepository.save(entity);
        return entityToDomain(savedEntity);
    }

    /**
     * Trova tutti i cambi di stato per una specifica annotazione
     */
    @Override
    public List<AnnotazioneStoricoStati> trovaStoricoPerAnnotazione(String idAnnotazione) {
        List<AnnotazioneStoricoStatiEntity> entities = storicoStatiRepository.findByIdAnnotazioneOrderByDataModificaDesc(idAnnotazione);
        return entities.stream()
                      .map(this::entityToDomain)
                      .collect(Collectors.toList());
    }

    /**
     * Converte entity in domain object
     */
    private AnnotazioneStoricoStati entityToDomain(AnnotazioneStoricoStatiEntity entity) {
        return new AnnotazioneStoricoStati(
            entity.getIdOperazione(),
            entity.getIdAnnotazione(),
            entity.getVersione(),
            entity.getStatoNew(),
            entity.getStatoOld(),
            entity.getUtente(),
            entity.getDataModifica(),
            entity.getNotaOperazione()
        );
    }
}
