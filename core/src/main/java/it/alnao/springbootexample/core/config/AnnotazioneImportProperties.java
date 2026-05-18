package it.alnao.springbootexample.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "gestione-annotazioni.import-annotazioni")
public class AnnotazioneImportProperties {

    private Kafka kafka = new Kafka();

    public static class Kafka {
        private String topicName = "annotazioni-import";

        public String getTopicName() { return topicName; }
        public void setTopicName(String topicName) { this.topicName = topicName; }
    }

    public Kafka getKafka() { return kafka; }
    public void setKafka(Kafka kafka) { this.kafka = kafka; }
}
