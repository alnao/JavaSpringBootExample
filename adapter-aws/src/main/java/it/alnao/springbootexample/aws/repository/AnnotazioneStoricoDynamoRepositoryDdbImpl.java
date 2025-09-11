package it.alnao.springbootexample.aws.repository;

import it.alnao.springbootexample.aws.entity.AnnotazioneStoricoDynamoEntity;
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
import java.util.stream.Collectors;

@Repository
@Profile("aws")
public class AnnotazioneStoricoDynamoRepositoryDdbImpl implements AnnotazioneStoricoDynamoRepository {
    private final DynamoDbTable<AnnotazioneStoricoDynamoEntity> table;

    @Autowired
    public AnnotazioneStoricoDynamoRepositoryDdbImpl(DynamoDbEnhancedClient dynamoDbEnhancedClient, NoSqlTableConfig tableConfig) {
        this.table = dynamoDbEnhancedClient.table(tableConfig.getAnnotazioniStoricoTableName(), TableSchema.fromBean(AnnotazioneStoricoDynamoEntity.class));
    }

    @Override
    public AnnotazioneStoricoDynamoEntity save(AnnotazioneStoricoDynamoEntity entity) {
        if (entity.getId() == null || entity.getId().isEmpty()) {
            entity.setId(java.util.UUID.randomUUID().toString());
        }
        table.putItem(entity);
        return entity;
    }

    @Override
    public List<AnnotazioneStoricoDynamoEntity> findByIdOriginale(String idOriginale) {
        // DynamoDB non supporta query su attributi non chiave senza GSI, quindi si fa scan e filtro in memoria
        PageIterable<AnnotazioneStoricoDynamoEntity> results = table.scan(ScanEnhancedRequest.builder().build());
        return results.items().stream()
                .filter(e -> idOriginale.equals(e.getIdOriginale()))
                .collect(Collectors.toList());
    }
}
