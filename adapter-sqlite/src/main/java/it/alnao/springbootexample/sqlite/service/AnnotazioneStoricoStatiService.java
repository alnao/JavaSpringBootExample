package it.alnao.springbootexample.sqlite.service;

import it.alnao.springbootexample.core.domain.AnnotazioneStoricoStati;
import it.alnao.springbootexample.sqlite.entity.AnnotazioneStoricoStatiSQLiteEntity;
import it.alnao.springbootexample.sqlite.repository.AnnotazioneStoricoStatiSQLiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service per gestire lo storico dei cambi di stato delle annotazioni (SQLite)
 */
@Service
@Profile("sqlite")
public class AnnotazioneStoricoStatiService implements it.alnao.springbootexample.core.service.AnnotazioneStoricoStatiService {

    @Autowired
    private AnnotazioneStoricoStatiSQLiteRepository storicoStatiRepository;

    /**
     * Inserisce un nuovo record di storico cambio stato
     */
    @Override
    public AnnotazioneStoricoStati inserisciCambioStato(String idAnnotazione, String versione, 
                                                       String statoNew, String statoOld, 
                                                       String utente, String notaOperazione) {
        String idOperazione = UUID.randomUUID().toString();
        LocalDateTime dataModifica = LocalDateTime.now();
        
        AnnotazioneStoricoStatiSQLiteEntity entity = new AnnotazioneStoricoStatiSQLiteEntity(
            idOperazione, idAnnotazione, versione, statoNew, statoOld, 
            utente, dataModifica, notaOperazione
        );
        
        AnnotazioneStoricoStatiSQLiteEntity savedEntity = storicoStatiRepository.save(entity);
        return entityToDomain(savedEntity);
    }

    /**
     * Trova tutti i cambi di stato per una specifica annotazione
     */
    @Override
    public List<AnnotazioneStoricoStati> trovaStoricoPerAnnotazione(String idAnnotazione) {
        List<AnnotazioneStoricoStatiSQLiteEntity> entities = storicoStatiRepository.findByIdAnnotazioneOrderByDataModificaDesc(idAnnotazione);
        return entities.stream()
                      .map(this::entityToDomain)
                      .collect(Collectors.toList());
    }

    /**
     * Converte entity in domain object
     */
    private AnnotazioneStoricoStati entityToDomain(AnnotazioneStoricoStatiSQLiteEntity entity) {
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
