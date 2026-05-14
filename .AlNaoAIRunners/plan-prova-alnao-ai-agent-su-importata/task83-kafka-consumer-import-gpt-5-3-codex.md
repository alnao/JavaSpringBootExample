Ho implementato nel modulo `adapter-kafka` un nuovo consumer `KafkaAnnotazioneImportConsumer` (`@KafkaListener` sul topic Kafka configurato) che deserializza `AnnotazioneCompleta` e aggiorna/salva i metadati con stato `IMPORTATA`.  
Ho esteso `KafkaConfig` aggiungendo configurazione consumer (`ConsumerFactory` + `ConcurrentKafkaListenerContainerFactory` + `@EnableKafka`) riusando broker e sicurezza già presenti.  
Ho aggiunto i test unitari in `KafkaAnnotazioneImportConsumerTest` e verificato con `mvn -q -pl adapter-kafka -am test -DskipITs` (passato).


