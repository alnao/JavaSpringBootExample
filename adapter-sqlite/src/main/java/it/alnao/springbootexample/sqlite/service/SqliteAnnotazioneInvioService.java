package it.alnao.springbootexample.sqlite.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.alnao.springbootexample.core.config.AnnotazioneInvioProperties;
import it.alnao.springbootexample.core.domain.AnnotazioneCompleta;
import it.alnao.springbootexample.core.domain.AnnotazioneMetadata;
import it.alnao.springbootexample.core.domain.StatoAnnotazione;
import it.alnao.springbootexample.core.repository.AnnotazioneMetadataRepository;
import it.alnao.springbootexample.core.repository.AnnotazioneRepository;
import it.alnao.springbootexample.core.service.AnnotazioneInvioService;
import it.alnao.springbootexample.sqlite.entity.AnnotazioneInviata;
import it.alnao.springbootexample.sqlite.repository.AnnotazioneInviataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Profile("sqlite")
public class SqliteAnnotazioneInvioService implements AnnotazioneInvioService {
    
    private static final Logger logger = LoggerFactory.getLogger(SqliteAnnotazioneInvioService.class);
    
    private final AnnotazioneMetadataRepository metadataRepository;
    private final AnnotazioneRepository annotazioneRepository;
    private final AnnotazioneInviataRepository annotazioneInviataRepository;
    private final AnnotazioneInvioProperties properties;
    private final ObjectMapper objectMapper;
    
    public SqliteAnnotazioneInvioService(AnnotazioneMetadataRepository metadataRepository,
                                       AnnotazioneRepository annotazioneRepository,
                                       AnnotazioneInviataRepository annotazioneInviataRepository,
                                       AnnotazioneInvioProperties properties,
                                       ObjectMapper objectMapper) {
        this.metadataRepository = metadataRepository;
        this.annotazioneRepository = annotazioneRepository;
        this.annotazioneInviataRepository = annotazioneInviataRepository;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }
    
    @Override
    @Transactional
    public List<AnnotazioneCompleta> inviaAnnotazioni() {
        List<AnnotazioneMetadata> metadatiDaInviare = metadataRepository.findByStato(StatoAnnotazione.DAINVIARE);
        List<AnnotazioneCompleta> annotazioniInviate = new ArrayList<>();
        
        for (AnnotazioneMetadata metadata : metadatiDaInviare) {
            try {
                // Recupera l'annotazione completa
                var annotazioneOpt = annotazioneRepository.findById(metadata.getId());
                if (annotazioneOpt.isEmpty()) {
                    logger.warn("Annotazione con ID {} non trovata", metadata.getId());
                    continue;
                }
                
                AnnotazioneCompleta annotazioneCompleta = new AnnotazioneCompleta(
                    annotazioneOpt.get(), metadata);
                
                // Serializza l'annotazione completa
                String contenutoJson = objectMapper.writeValueAsString(annotazioneCompleta);
                
                // Crea l'entity per la tabella
                AnnotazioneInviata annotazioneInviata = new AnnotazioneInviata(
                        metadata.getId(),
                        contenutoJson,
                        LocalDateTime.now(),
                        "INVIATA"
                );
                
                // Salva nella tabella annotazioni_inviate
                annotazioneInviataRepository.save(annotazioneInviata);
                
                // Aggiorna lo stato a INVIATA
                metadata.setStato(StatoAnnotazione.INVIATA.getValue());
                metadataRepository.save(metadata);
                
                annotazioniInviate.add(annotazioneCompleta);
                
                logger.debug("Annotazione {} salvata nella tabella {} con ID {}", 
                    metadata.getId(), 
                    properties.getSqlite().getTableName(),
                    annotazioneInviata.getId());
                
            } catch (Exception e) {
                logger.error("Errore durante il salvataggio dell'annotazione {}", metadata.getId(), e);
                
                // Salva l'errore nella tabella
                try {
                    AnnotazioneInviata annotazioneErrore = new AnnotazioneInviata(
                        metadata.getId(),
                        null,
                        LocalDateTime.now(),
                        "ERRORE"
                    );
                    annotazioneErrore.setMessaggioErrore(e.getMessage());
                    annotazioneInviataRepository.save(annotazioneErrore);
                } catch (Exception ex) {
                    logger.error("Errore anche nel salvataggio dell'errore per annotazione {}", metadata.getId(), ex);
                }
                
                // Imposta lo stato di errore
                metadata.setStato(StatoAnnotazione.ERRORE.getValue());
                metadataRepository.save(metadata);
            }
        }
        
        return annotazioniInviate;
    }
    
    @Override
    public boolean isEnabled() {
        return properties.isEnabled();
    }
}
