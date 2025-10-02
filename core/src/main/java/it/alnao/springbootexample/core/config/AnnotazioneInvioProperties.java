package it.alnao.springbootexample.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "gestione-annotazioni.export-annotazioni")
public class AnnotazioneInvioProperties {
    
    private boolean enabled = false;
    private String cronExpression = "0 */5 * * * *"; // ogni 5 minuti di default
    private Kafka kafka = new Kafka();
    private Sqlite sqlite = new Sqlite();
    
    public static class Kafka {
        private String brokerUrl = "localhost:9092";
        private String topicName = "annotazioni-inviate";
        
        // getters and setters
        public String getBrokerUrl() { return brokerUrl; }
        public void setBrokerUrl(String brokerUrl) { this.brokerUrl = brokerUrl; }
        public String getTopicName() { return topicName; }
        public void setTopicName(String topicName) { this.topicName = topicName; }
    }
    
    public static class Sqlite {
        private String tableName = "annotazioni_inviate";
        
        // getters and setters
        public String getTableName() { return tableName; }
        public void setTableName(String tableName) { this.tableName = tableName; }
    }
    
    // getters and setters
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getCronExpression() { return cronExpression; }
    public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }
    public Kafka getKafka() { return kafka; }
    public void setKafka(Kafka kafka) { this.kafka = kafka; }
    public Sqlite getSqlite() { return sqlite; }
    public void setSqlite(Sqlite sqlite) { this.sqlite = sqlite; }
}
