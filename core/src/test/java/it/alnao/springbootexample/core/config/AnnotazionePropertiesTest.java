package it.alnao.springbootexample.core.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnnotazionePropertiesTest {

    // ---------- AnnotazioneInvioProperties ----------

    @Test
    void invioProperties_defaultsAreDisabledWithAFiveMinuteCron() {
        AnnotazioneInvioProperties p = new AnnotazioneInvioProperties();
        assertFalse(p.isEnabled());
        assertEquals("0 */5 * * * *", p.getCronExpression());
        assertNotNull(p.getKafka());
        assertNotNull(p.getSqlite());
    }

    @Test
    void invioProperties_settersAndGettersWork() {
        AnnotazioneInvioProperties p = new AnnotazioneInvioProperties();
        p.setEnabled(true);
        p.setCronExpression("0 0 * * * *");
        assertTrue(p.isEnabled());
        assertEquals("0 0 * * * *", p.getCronExpression());
    }

    @Test
    void invioProperties_kafkaDefaultsPointToLocalhost() {
        AnnotazioneInvioProperties.Kafka kafka = new AnnotazioneInvioProperties().getKafka();
        assertEquals("localhost:9092", kafka.getBrokerUrl());
        assertEquals("annotazioni-inviate", kafka.getTopicName());
        assertEquals("", kafka.getSaslMechanism());
        assertEquals("", kafka.getSaslJaasConfig());
        assertEquals("", kafka.getSecurityProtocol());
    }

    @Test
    void invioProperties_kafkaSettersAndGettersWork() {
        AnnotazioneInvioProperties.Kafka kafka = new AnnotazioneInvioProperties.Kafka();
        kafka.setBrokerUrl("broker:9093");
        kafka.setTopicName("topic-custom");
        kafka.setSaslMechanism("PLAIN");
        kafka.setSaslJaasConfig("jaas-config");
        kafka.setSecurityProtocol("SASL_SSL");

        assertEquals("broker:9093", kafka.getBrokerUrl());
        assertEquals("topic-custom", kafka.getTopicName());
        assertEquals("PLAIN", kafka.getSaslMechanism());
        assertEquals("jaas-config", kafka.getSaslJaasConfig());
        assertEquals("SASL_SSL", kafka.getSecurityProtocol());
    }

    @Test
    void invioProperties_kafkaBlockCanBeReplaced() {
        AnnotazioneInvioProperties p = new AnnotazioneInvioProperties();
        AnnotazioneInvioProperties.Kafka kafka = new AnnotazioneInvioProperties.Kafka();
        kafka.setTopicName("nuovo-topic");
        p.setKafka(kafka);
        assertEquals("nuovo-topic", p.getKafka().getTopicName());
    }

    @Test
    void invioProperties_sqliteDefaultsAndSetters() {
        AnnotazioneInvioProperties p = new AnnotazioneInvioProperties();
        assertEquals("annotazioni_inviate", p.getSqlite().getTableName());

        AnnotazioneInvioProperties.Sqlite sqlite = new AnnotazioneInvioProperties.Sqlite();
        sqlite.setTableName("tabella_custom");
        p.setSqlite(sqlite);
        assertEquals("tabella_custom", p.getSqlite().getTableName());
    }

    // ---------- AnnotazioneImportProperties ----------

    @Test
    void importProperties_defaultsAreDisabledWithAFiveMinuteCron() {
        AnnotazioneImportProperties p = new AnnotazioneImportProperties();
        assertFalse(p.isEnabled());
        assertEquals("0 */5 * * * *", p.getCronExpression());
        assertNotNull(p.getKafka());
        assertNotNull(p.getAws());
    }

    @Test
    void importProperties_settersAndGettersWork() {
        AnnotazioneImportProperties p = new AnnotazioneImportProperties();
        p.setEnabled(true);
        p.setCronExpression("0 */2 * * * *");
        assertTrue(p.isEnabled());
        assertEquals("0 */2 * * * *", p.getCronExpression());
    }

    @Test
    void importProperties_kafkaTopicDefaultAndSetter() {
        AnnotazioneImportProperties p = new AnnotazioneImportProperties();
        assertEquals("annotazioni-import", p.getKafka().getTopicName());

        AnnotazioneImportProperties.Kafka kafka = new AnnotazioneImportProperties.Kafka();
        kafka.setTopicName("import-custom");
        p.setKafka(kafka);
        assertEquals("import-custom", p.getKafka().getTopicName());
    }

    @Test
    void importProperties_sqsDefaultsAndSetters() {
        AnnotazioneImportProperties p = new AnnotazioneImportProperties();
        assertEquals(10, p.getAws().getSqs().getMaxNumberOfMessages());

        AnnotazioneImportProperties.Sqs sqs = new AnnotazioneImportProperties.Sqs();
        sqs.setMaxNumberOfMessages(5);
        sqs.setWaitTimeSeconds(20);
        assertEquals(5, sqs.getMaxNumberOfMessages());
        assertEquals(20, sqs.getWaitTimeSeconds());

        AnnotazioneImportProperties.Aws aws = new AnnotazioneImportProperties.Aws();
        aws.setSqs(sqs);
        p.setAws(aws);
        assertEquals(5, p.getAws().getSqs().getMaxNumberOfMessages());
    }
}
