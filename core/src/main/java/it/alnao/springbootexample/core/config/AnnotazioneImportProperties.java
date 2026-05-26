package it.alnao.springbootexample.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "gestione-annotazioni.import-annotazioni")
public class AnnotazioneImportProperties {

    private boolean enabled = false;
    private String cronExpression = "0 */5 * * * *";
    private Kafka kafka = new Kafka();
    private Aws aws = new Aws();

    public static class Kafka {
        private String topicName = "annotazioni-import";

        public String getTopicName() { return topicName; }
        public void setTopicName(String topicName) { this.topicName = topicName; }
    }

    public static class Aws {
        private Sqs sqs = new Sqs();

        public Sqs getSqs() { return sqs; }
        public void setSqs(Sqs sqs) { this.sqs = sqs; }
    }

    public static class Sqs {
        private Integer maxNumberOfMessages = 10;
        private Integer waitTimeSeconds = 5;

        public Integer getMaxNumberOfMessages() { return maxNumberOfMessages; }
        public void setMaxNumberOfMessages(Integer maxNumberOfMessages) { this.maxNumberOfMessages = maxNumberOfMessages; }
        public Integer getWaitTimeSeconds() { return waitTimeSeconds; }
        public void setWaitTimeSeconds(Integer waitTimeSeconds) { this.waitTimeSeconds = waitTimeSeconds; }
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getCronExpression() { return cronExpression; }
    public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }
    public Kafka getKafka() { return kafka; }
    public void setKafka(Kafka kafka) { this.kafka = kafka; }
    public Aws getAws() { return aws; }
    public void setAws(Aws aws) { this.aws = aws; }
}
