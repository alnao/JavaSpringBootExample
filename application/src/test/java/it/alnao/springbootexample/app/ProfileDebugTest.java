package it.alnao.springbootexample.app;

import java.util.logging.Logger;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

@SpringBootTest
@ActiveProfiles("sqlite")
public class ProfileDebugTest {
    Logger logger = Logger.getLogger(ProfileDebugTest.class.getName());
    
    @Autowired
    private ApplicationContext context;
    
    @Autowired
    private Environment environment;

    @Test
    public void debugProfiles() {
        logger.info("Active profiles: " + java.util.Arrays.toString(environment.getActiveProfiles()));
        logger.info("Bean definitions count: " + context.getBeanDefinitionCount());
        
        // Try to find our repositories
        try {
            String[] annotazioneRepoBeans = context.getBeanNamesForType(it.alnao.springbootexample.core.repository.AnnotazioneRepository.class);
            logger.info("AnnotazioneRepository beans: " + java.util.Arrays.toString(annotazioneRepoBeans));
        } catch (Exception e) {
            logger.severe("Error finding AnnotazioneRepository: " + e.getMessage());
        }
        
        try {
            String[] metadataRepoBeans = context.getBeanNamesForType(it.alnao.springbootexample.core.repository.AnnotazioneMetadataRepository.class);
            logger.info("AnnotazioneMetadataRepository beans: " + java.util.Arrays.toString(metadataRepoBeans));
        } catch (Exception e) {
            logger.severe("Error finding AnnotazioneMetadataRepository: " + e.getMessage());
        }

        // Check for SQLite specific beans
        try {
            String[] sqliteBeans = context.getBeanNamesForType(it.alnao.springbootexample.sqlite.service.SqliteAnnotazioneInvioService.class);
            logger.info("SqliteAnnotazioneInvioService beans: " + java.util.Arrays.toString(sqliteBeans));
        } catch (Exception e) {
            logger.severe("Error finding SqliteAnnotazioneInvioService: " + e.getMessage());
        }
    }
}
