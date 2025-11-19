package it.alnao.springbootexample.kafka.config;

import it.alnao.springbootexample.core.config.AnnotazioneInvioProperties;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Profile("kube")
public class KafkaConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(KafkaConfig.class);
    
    private final AnnotazioneInvioProperties properties;
    
    public KafkaConfig(AnnotazioneInvioProperties properties) {
        this.properties = properties;
    }
    
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        
        String brokerUrl = properties.getKafka().getBrokerUrl();
        logger.info("[KafkaConfig] Configurazione Kafka Producer - Broker URL: {}", brokerUrl);
        
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrl);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        configProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        configProps.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 60000);

        // Configurazioni di sicurezza per Azure Event Hubs
        String saslMechanism = properties.getKafka().getSaslMechanism();
        String saslJaasConfig = properties.getKafka().getSaslJaasConfig();
        String securityProtocol = properties.getKafka().getSecurityProtocol();
        
        logger.debug("[KafkaConfig] SASL Mechanism: {}", saslMechanism);
        logger.debug("[KafkaConfig] Security Protocol: {}", securityProtocol);
        logger.debug("[KafkaConfig] SASL JAAS Config presente: {}", saslJaasConfig != null && !saslJaasConfig.isEmpty());
        
        if (saslMechanism != null && !saslMechanism.isEmpty()) {
            configProps.put(SaslConfigs.SASL_MECHANISM, saslMechanism);
            logger.debug("[KafkaConfig] Configurato SASL Mechanism: {}", saslMechanism);
        }
        
        if (saslJaasConfig != null && !saslJaasConfig.isEmpty()) {
            configProps.put(SaslConfigs.SASL_JAAS_CONFIG, saslJaasConfig);
            logger.debug("[KafkaConfig] Configurato SASL JAAS Config (lunghezza: {})", saslJaasConfig.length());
        } else {
            logger.error("[KafkaConfig] SASL JAAS Config è NULL o vuota! Questo causerà errori di autenticazione.");
        }
        
        if (securityProtocol != null && !securityProtocol.isEmpty()) {
            configProps.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocol);
            logger.debug("[KafkaConfig] Configurato Security Protocol: {}", securityProtocol);
        }

        return new DefaultKafkaProducerFactory<>(configProps);
    }
    
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}