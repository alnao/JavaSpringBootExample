package it.alnao.springbootexample.core.scheduler;

import it.alnao.springbootexample.core.config.AnnotazioneInvioProperties;
import it.alnao.springbootexample.core.service.AnnotazioneInvioService;
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
    private final AnnotazioneInvioProperties properties;
    
    public AnnotazioneInvioScheduler(AnnotazioneInvioService annotazioneInvioService, 
                                   AnnotazioneInvioProperties properties) {
        this.annotazioneInvioService = annotazioneInvioService;
        this.properties = properties;
    }
    
    @Scheduled(cron = "#{@annotazioneInvioProperties.cronExpression}")
    public void inviaAnnotazioni() {
        if (!annotazioneInvioService.isEnabled()) {
            logger.debug("Servizio di invio annotazioni disabilitato");
            return;
        }
        
        try {
            var annotazioniInviate = annotazioneInvioService.inviaAnnotazioni();
            logger.info("Inviate {} annotazioni", annotazioniInviate.size());
        } catch (Exception e) {
            logger.error("Errore durante l'invio delle annotazioni", e);
        }
    }
}
