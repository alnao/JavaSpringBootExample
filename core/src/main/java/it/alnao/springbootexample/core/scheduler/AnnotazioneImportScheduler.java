package it.alnao.springbootexample.core.scheduler;

import it.alnao.springbootexample.core.service.AnnotazioneImportService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(AnnotazioneImportService.class)
@ConditionalOnProperty(value = "gestione-annotazioni.import-annotazioni.enabled", havingValue = "true")
public class AnnotazioneImportScheduler {

    private static final Logger logger = LoggerFactory.getLogger(AnnotazioneImportScheduler.class);

    private final AnnotazioneImportService annotazioneImportService;

    public AnnotazioneImportScheduler(AnnotazioneImportService annotazioneImportService) {
        this.annotazioneImportService = annotazioneImportService;
    }

    @Scheduled(cron = "#{@annotazioneImportProperties.cronExpression}")
    @SchedulerLock(
        name = "scheduler-import-annotations",
        lockAtLeastFor = "PT30S",
        lockAtMostFor = "PT3M"
    )
    public void importaAnnotazioni() {
        if (!annotazioneImportService.isEnabled()) {
            logger.debug("[AnnotazioneImportScheduler] Servizio import annotazioni disabilitato");
            return;
        }

        logger.debug("[AnnotazioneImportScheduler] Avvio import annotazioni tramite scheduler");
        try {
            int imported = annotazioneImportService.importaAnnotazioni().size();
            logger.info("[AnnotazioneImportScheduler] Importate {} annotazioni", imported);
        } catch (Exception e) {
            logger.error("[AnnotazioneImportScheduler] Errore durante l'import delle annotazioni", e);
        }
    }
}
