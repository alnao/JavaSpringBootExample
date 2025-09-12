package it.alnao.springbootexample.aws.repository;

import it.alnao.springbootexample.aws.entity.AnnotazioneStoricoStatiDynamoEntity;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repository per lo storico dei cambi di stato delle annotazioni in DynamoDB
 */
@Repository
@Profile("aws")
public class AnnotazioneStoricoStatiDynamoRepository {

    private final DynamoDbTable<AnnotazioneStoricoStatiDynamoEntity> table;

    public AnnotazioneStoricoStatiDynamoRepository(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.table = dynamoDbEnhancedClient.table("annotazioni_storicoStati", 
                TableSchema.fromBean(AnnotazioneStoricoStatiDynamoEntity.class));
    }

    /**
     * Salva un nuovo record di storico
     */
    public AnnotazioneStoricoStatiDynamoEntity save(AnnotazioneStoricoStatiDynamoEntity entity) {
        table.putItem(entity);
        return entity;
    }

    /**
     * Trova per ID operazione
     */
    public Optional<AnnotazioneStoricoStatiDynamoEntity> findById(String idOperazione) {
        AnnotazioneStoricoStatiDynamoEntity entity = table.getItem(r -> r.key(k -> k.partitionValue(idOperazione)));
        return Optional.ofNullable(entity);
    }

    /**
     * Trova tutti i record di storico per una specifica annotazione
     * Utilizza il GSI (Global Secondary Index) su idAnnotazione
     */
    public List<AnnotazioneStoricoStatiDynamoEntity> findByIdAnnotazione(String idAnnotazione) {
        // Nota: Questo richiede un GSI configurato su idAnnotazione
        // Per ora restituiamo una lista vuota, la implementazione completa richiede
        // la configurazione del GSI nel setup DynamoDB
        return List.of();
    }

    /**
     * Trova tutti i record
     */
    public List<AnnotazioneStoricoStatiDynamoEntity> findAll() {
        return table.scan().items().stream().collect(Collectors.toList());
    }

    /**
     * Elimina per ID operazione
     */
    public void deleteById(String idOperazione) {
        table.deleteItem(r -> r.key(k -> k.partitionValue(idOperazione)));
    }

    /**
     * Verifica se esiste un record con l'ID operazione specificato
     */
    public boolean existsById(String idOperazione) {
        return findById(idOperazione).isPresent();
    }

    /**
     * Conta tutti i record
     */
    public long count() {
        return table.scan().items().stream().count();
    }
}
