package it.alnao.springbootexample.aws.repository;

import it.alnao.springbootexample.aws.entity.AnnotazioneDynamoEntity;
import it.alnao.springbootexample.core.config.NoSqlTableConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Profile("aws")
public class AnnotazioneDynamoRepository {

    private final DynamoDbTable<AnnotazioneDynamoEntity> table;

    @Autowired
    public AnnotazioneDynamoRepository(DynamoDbEnhancedClient dynamoDbEnhancedClient, NoSqlTableConfig tableConfig) {
        this.table = dynamoDbEnhancedClient.table(tableConfig.getAnnotazioniTableName(), TableSchema.fromBean(AnnotazioneDynamoEntity.class));
    }

    public AnnotazioneDynamoEntity save(AnnotazioneDynamoEntity entity) {
        table.putItem(entity);
        return entity;
    }

    public Optional<AnnotazioneDynamoEntity> findById(String id) {
        AnnotazioneDynamoEntity result = table.getItem(r -> r.key(k -> k.partitionValue(id)));
        return Optional.ofNullable(result);
    }

    public List<AnnotazioneDynamoEntity> findAll() {
        PageIterable<AnnotazioneDynamoEntity> results = table.scan(ScanEnhancedRequest.builder().build());
        return results.items().stream().collect(Collectors.toList());
    }

    public void deleteById(String id) {
        table.deleteItem(r -> r.key(k -> k.partitionValue(id)));
    }

    public boolean existsById(String id) {
        return findById(id).isPresent();
    }

    public List<AnnotazioneDynamoEntity> findByValoreNotaContaining(String valoreNota) {
        // Per semplicità, scan completa e filtro in memoria
        // In produzione, usare indici globali secondari per performance migliori
        return findAll().stream()
                .filter(entity -> entity.getValoreNota() != null && 
                                entity.getValoreNota().toLowerCase().contains(valoreNota.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<AnnotazioneDynamoEntity> findByVersioneNota(String versioneNota) {
        return findAll().stream()
                .filter(entity -> versioneNota.equals(entity.getVersioneNota()))
                .collect(Collectors.toList());
    }

    public long count() {
        return findAll().size(); // In produzione, usare CloudWatch metrics per performance migliori
    }
}
