package it.alnao.springbootexample.core.scheduler;

import it.alnao.springbootexample.core.config.AnnotazioneInvioProperties;
import it.alnao.springbootexample.core.domain.AnnotazioneCompleta;
import it.alnao.springbootexample.core.domain.StatoAnnotazione;
import it.alnao.springbootexample.core.service.AnnotazioneInvioService;
import it.alnao.springbootexample.core.service.AnnotazioneStoricoStatiService;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "gestione-annotazioni.export-annotazioni.enabled", havingValue = "true")
public class AnnotazioneInvioScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(AnnotazioneInvioScheduler.class);
    
    private final AnnotazioneInvioService annotazioneInvioService;
    private final AnnotazioneStoricoStatiService annotazioneStoricoStatiService;
    private final AnnotazioneInvioProperties properties;
    
    public AnnotazioneInvioScheduler(AnnotazioneInvioService annotazioneInvioService, 
                                   AnnotazioneStoricoStatiService annotazioneStoricoStatiService,
                                   AnnotazioneInvioProperties properties) {
        this.annotazioneInvioService = annotazioneInvioService;
        this.annotazioneStoricoStatiService = annotazioneStoricoStatiService;
        this.properties = properties;
    }
    
    @Scheduled(cron = "#{@annotazioneInvioProperties.cronExpression}")
    public void inviaAnnotazioni() {
        if (!annotazioneInvioService.isEnabled()) {
            logger.debug("Servizio di invio annotazioni disabilitato");
            return;
        }
        
        try {
            List<AnnotazioneCompleta> annotazioniInviate = annotazioneInvioService.inviaAnnotazioni();
            annotazioniInviate.forEach(annotazione -> {
                //logger.debug("Annotazione inviata: {}", annotazione)
                annotazioneStoricoStatiService.inserisciCambioStato(
                    annotazione.getId().toString(),
                    annotazione.getAnnotazione().getVersioneNota(),
                    StatoAnnotazione.INVIATA.getValue(),
                    annotazione.getMetadata().getStato(),
                    "CRON Scheduler",
                    "Invio automatico tramite scheduler " + System.currentTimeMillis()
                );
            });
            logger.info("Inviate {} annotazioni", annotazioniInviate.size());
           
        } catch (Exception e) {
            logger.error("Errore durante l'invio delle annotazioni", e);
        }
    }
}
