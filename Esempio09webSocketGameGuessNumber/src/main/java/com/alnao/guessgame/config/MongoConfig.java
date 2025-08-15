package com.alnao.guessgame.config;

import org.springframework.context.annotation.Configuration;

/**
 * MongoDB configuration
 * Note: We don't extend AbstractMongoClientConfiguration to allow Spring Boot
 * auto-configuration to use the spring.data.mongodb.uri property from application.yml
 */
@Configuration
public class MongoConfig {


    // Additional MongoDB configuration can be added here if needed
    // For example, custom converters, validators, etc.
    // But let Spring Boot auto-configuration handle the connection
}

/*
Il problema è nel file `MongoConfig.java` che stai visualizzando! 
Questo file sta sovrascrivendo la configurazione di Spring Boot e forzando l'uso del database 
"guessgame" senza utilizzare l'URI specificato nelle variabili d'ambiente.
*/
    
/*package com.alnao.guessgame.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {
    
    @Override
    protected String getDatabaseName() {
        return "guessgame";
    }
    
    // Additional MongoDB configuration can be added here
    // For example, custom converters, connection settings, etc.
}*/