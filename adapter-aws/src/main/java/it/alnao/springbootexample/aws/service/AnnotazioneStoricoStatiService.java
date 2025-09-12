package it.alnao.springbootexample.aws.service;

import it.alnao.springbootexample.core.domain.AnnotazioneStoricoStati;
import it.alnao.springbootexample.aws.entity.AnnotazioneStoricoStatiDynamoEntity;
import it.alnao.springbootexample.aws.repository.AnnotazioneStoricoStatiDynamoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service per gestire lo storico dei cambi di stato delle annotazioni (DynamoDB)
 */
@Service
@Profile("aws")
public class AnnotazioneStoricoStatiService implements it.alnao.springbootexample.core.service.AnnotazioneStoricoStatiService {

    @Autowired
    private AnnotazioneStoricoStatiDynamoRepository storicoStatiRepository;

    /**
     * Inserisce un nuovo record di storico cambio stato
     */
    @Override
    public AnnotazioneStoricoStati inserisciCambioStato(String idAnnotazione, String versione, 
                                                       String statoNew, String statoOld, 
                                                       String utente, String notaOperazione) {
        String idOperazione = UUID.randomUUID().toString();
        String dataModifica = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        
        AnnotazioneStoricoStatiDynamoEntity entity = new AnnotazioneStoricoStatiDynamoEntity(
            idOperazione, idAnnotazione, versione, statoNew, statoOld, 
            utente, dataModifica, notaOperazione
        );
        
        AnnotazioneStoricoStatiDynamoEntity savedEntity = storicoStatiRepository.save(entity);
        return entityToDomain(savedEntity);
    }

    /**
     * Trova tutti i cambi di stato per una specifica annotazione
     * Nota: La implementazione completa richiede un GSI configurato su idAnnotazione
     */
    @Override
    public List<AnnotazioneStoricoStati> trovaStoricoPerAnnotazione(String idAnnotazione) {
        List<AnnotazioneStoricoStatiDynamoEntity> entities = storicoStatiRepository.findByIdAnnotazione(idAnnotazione);
        return entities.stream()
                      .map(this::entityToDomain)
                      .collect(Collectors.toList());
    }

    /**
     * Converte entity in domain object
     */
    private AnnotazioneStoricoStati entityToDomain(AnnotazioneStoricoStatiDynamoEntity entity) {
        LocalDateTime dataModifica = null;
        try {
            dataModifica = LocalDateTime.parse(entity.getDataModifica(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            dataModifica = LocalDateTime.now(); // fallback
        }
        
        return new AnnotazioneStoricoStati(
            entity.getIdOperazione(),
            entity.getIdAnnotazione(),
            entity.getVersione(),
            entity.getStatoNew(),
            entity.getStatoOld(),
            entity.getUtente(),
            dataModifica,
            entity.getNotaOperazione()
        );
    }
}
