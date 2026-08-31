package it.alnao.springbootexample.kafka.config;

import it.alnao.springbootexample.core.config.AnnotazioneInvioProperties;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class KafkaConfigTest {

    private AnnotazioneInvioProperties properties;
    private KafkaConfig config;

    @BeforeEach
    void setup() {
        properties = new AnnotazioneInvioProperties();
        properties.getKafka().setBrokerUrl("broker-test:9092");
        config = new KafkaConfig(properties);
    }

    private Map<String, Object> producerProps() {
        return config.producerFactory().getConfigurationProperties();
    }

    private Map<String, Object> consumerProps() {
        return config.consumerFactory().getConfigurationProperties();
    }

    // ---------- producerFactory ----------

    @Test
    void producerFactory_usesTheConfiguredBrokerAndReliableDefaults() {
        Map<String, Object> props = producerProps();
        assertEquals("broker-test:9092", props.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals("all", props.get(ProducerConfig.ACKS_CONFIG));
        assertEquals(3, props.get(ProducerConfig.RETRIES_CONFIG));
        assertEquals(16384, props.get(ProducerConfig.BATCH_SIZE_CONFIG));
        assertEquals(1, props.get(ProducerConfig.LINGER_MS_CONFIG));
        assertEquals(33554432, props.get(ProducerConfig.BUFFER_MEMORY_CONFIG));
        assertEquals(30000, props.get(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG));
        assertEquals(60000, props.get(ProducerConfig.MAX_BLOCK_MS_CONFIG));
    }

    @Test
    void producerFactory_withoutSaslLeavesTheSecurityKeysOut() {
        Map<String, Object> props = producerProps();
        assertFalse(props.containsKey(SaslConfigs.SASL_MECHANISM));
        assertFalse(props.containsKey(SaslConfigs.SASL_JAAS_CONFIG));
        assertFalse(props.containsKey(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG));
    }

    @Test
    void producerFactory_withSaslAppliesEverySecurityKey() {
        properties.getKafka().setSaslMechanism("PLAIN");
        properties.getKafka().setSaslJaasConfig("org.apache.kafka.common.security.plain.PlainLoginModule required;");
        properties.getKafka().setSecurityProtocol("SASL_SSL");

        Map<String, Object> props = producerProps();

        assertEquals("PLAIN", props.get(SaslConfigs.SASL_MECHANISM));
        assertEquals("org.apache.kafka.common.security.plain.PlainLoginModule required;",
                props.get(SaslConfigs.SASL_JAAS_CONFIG));
        assertEquals("SASL_SSL", props.get(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG));
    }

    @Test
    void producerFactory_withNullSaslValuesLeavesTheSecurityKeysOut() {
        properties.getKafka().setSaslMechanism(null);
        properties.getKafka().setSaslJaasConfig(null);
        properties.getKafka().setSecurityProtocol(null);

        Map<String, Object> props = producerProps();

        assertFalse(props.containsKey(SaslConfigs.SASL_MECHANISM));
        assertFalse(props.containsKey(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG));
    }

    @Test
    void producerFactory_returnsAUsableFactory() {
        ProducerFactory<String, String> factory = config.producerFactory();
        assertNotNull(factory);
    }

    // ---------- consumerFactory ----------

    @Test
    void consumerFactory_usesTheConfiguredBrokerAndGroup() {
        Map<String, Object> props = consumerProps();
        assertEquals("broker-test:9092", props.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals("annotazioni-import-consumer", props.get(ConsumerConfig.GROUP_ID_CONFIG));
        assertEquals("earliest", props.get(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG));
        assertEquals(true, props.get(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG));
    }

    @Test
    void consumerFactory_withSaslAppliesEverySecurityKey() {
        properties.getKafka().setSaslMechanism("SCRAM-SHA-512");
        properties.getKafka().setSaslJaasConfig("jaas-config");
        properties.getKafka().setSecurityProtocol("SASL_PLAINTEXT");

        Map<String, Object> props = consumerProps();

        assertEquals("SCRAM-SHA-512", props.get(SaslConfigs.SASL_MECHANISM));
        assertEquals("jaas-config", props.get(SaslConfigs.SASL_JAAS_CONFIG));
        assertEquals("SASL_PLAINTEXT", props.get(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG));
    }

    @Test
    void consumerFactory_withoutSaslLeavesTheSecurityKeysOut() {
        Map<String, Object> props = consumerProps();
        assertFalse(props.containsKey(SaslConfigs.SASL_MECHANISM));
        assertFalse(props.containsKey(SaslConfigs.SASL_JAAS_CONFIG));
    }

    @Test
    void consumerFactory_returnsAUsableFactory() {
        ConsumerFactory<String, String> factory = config.consumerFactory();
        assertNotNull(factory);
    }

    // ---------- listener e template ----------

    @Test
    void kafkaListenerContainerFactory_isWiredToTheConsumerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                config.kafkaListenerContainerFactory();
        assertNotNull(factory);
        assertNotNull(factory.getConsumerFactory());
    }

    @Test
    void kafkaTemplate_isWiredToTheProducerFactory() {
        KafkaTemplate<String, String> template = config.kafkaTemplate();
        assertNotNull(template);
        assertNotNull(template.getProducerFactory());
    }

    @Test
    void consumerFactory_withOnlyTheMechanismSet_appliesJustThatKey() {
        properties.getKafka().setSaslMechanism("PLAIN");
        properties.getKafka().setSaslJaasConfig("");
        properties.getKafka().setSecurityProtocol("");

        Map<String, Object> props = consumerProps();

        assertEquals("PLAIN", props.get(SaslConfigs.SASL_MECHANISM));
        assertFalse(props.containsKey(SaslConfigs.SASL_JAAS_CONFIG));
        assertFalse(props.containsKey(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG));
    }

    @Test
    void consumerFactory_withNullSaslValuesLeavesTheSecurityKeysOut() {
        properties.getKafka().setSaslMechanism(null);
        properties.getKafka().setSaslJaasConfig(null);
        properties.getKafka().setSecurityProtocol(null);

        Map<String, Object> props = consumerProps();

        assertFalse(props.containsKey(SaslConfigs.SASL_MECHANISM));
        assertFalse(props.containsKey(SaslConfigs.SASL_JAAS_CONFIG));
        assertFalse(props.containsKey(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG));
    }

    @Test
    void producerFactory_withOnlyTheProtocolSet_appliesJustThatKey() {
        properties.getKafka().setSaslMechanism("");
        properties.getKafka().setSaslJaasConfig("");
        properties.getKafka().setSecurityProtocol("SSL");

        Map<String, Object> props = producerProps();

        assertEquals("SSL", props.get(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG));
        assertFalse(props.containsKey(SaslConfigs.SASL_MECHANISM));
    }
}
