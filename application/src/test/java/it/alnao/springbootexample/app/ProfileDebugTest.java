package it.alnao.springbootexample.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

@SpringBootTest
@ActiveProfiles("sqlite")
public class ProfileDebugTest {

    @Autowired
    private ApplicationContext context;
    
    @Autowired
    private Environment environment;

    @Test
    public void debugProfiles() {
        System.out.println("Active profiles: " + java.util.Arrays.toString(environment.getActiveProfiles()));
        System.out.println("Bean definitions count: " + context.getBeanDefinitionCount());
        
        // Try to find our repositories
        try {
            String[] annotazioneRepoBeans = context.getBeanNamesForType(it.alnao.springbootexample.core.repository.AnnotazioneRepository.class);
            System.out.println("AnnotazioneRepository beans: " + java.util.Arrays.toString(annotazioneRepoBeans));
        } catch (Exception e) {
            System.out.println("Error finding AnnotazioneRepository: " + e.getMessage());
        }
        
        try {
            String[] metadataRepoBeans = context.getBeanNamesForType(it.alnao.springbootexample.core.repository.AnnotazioneMetadataRepository.class);
            System.out.println("AnnotazioneMetadataRepository beans: " + java.util.Arrays.toString(metadataRepoBeans));
        } catch (Exception e) {
            System.out.println("Error finding AnnotazioneMetadataRepository: " + e.getMessage());
        }

        // Check for SQLite specific beans
        try {
            String[] sqliteBeans = context.getBeanNamesForType(it.alnao.springbootexample.sqlite.service.SqliteAnnotazioneInvioService.class);
            System.out.println("SqliteAnnotazioneInvioService beans: " + java.util.Arrays.toString(sqliteBeans));
        } catch (Exception e) {
            System.out.println("Error finding SqliteAnnotazioneInvioService: " + e.getMessage());
        }
    }
}
