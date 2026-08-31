package it.alnao.springbootexample.aws.repository;

import it.alnao.springbootexample.aws.entity.AnnotazioneStoricoDynamoEntity;
import it.alnao.springbootexample.core.config.NoSqlTableConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class AnnotazioneStoricoDynamoRepositoryDdbImplTest {

    @Mock DynamoDbEnhancedClient enhancedClient;
    @Mock DynamoDbTable<AnnotazioneStoricoDynamoEntity> table;
    @Mock PageIterable<AnnotazioneStoricoDynamoEntity> pageIterable;

    private AnnotazioneStoricoDynamoRepositoryDdbImpl repository;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        NoSqlTableConfig tableConfig = new NoSqlTableConfig();
        tableConfig.setAnnotazioniStoricoTableName("annotazioni-storico-test");
        doReturn(table).when(enhancedClient).table(eq("annotazioni-storico-test"), any());
        repository = new AnnotazioneStoricoDynamoRepositoryDdbImpl(enhancedClient, tableConfig);
    }

    private AnnotazioneStoricoDynamoEntity entity(String id, String idOriginale) {
        AnnotazioneStoricoDynamoEntity e = new AnnotazioneStoricoDynamoEntity();
        e.setId(id);
        e.setIdOriginale(idOriginale);
        e.setValoreNota("testo");
        return e;
    }

    private void tableContains(AnnotazioneStoricoDynamoEntity... entities) {
        SdkIterable<AnnotazioneStoricoDynamoEntity> items = () -> List.of(entities).iterator();
        doReturn(items).when(pageIterable).items();
        doReturn(pageIterable).when(table).scan(any(ScanEnhancedRequest.class));
    }

    @Test
    void save_generatesAnIdWhenMissing() {
        AnnotazioneStoricoDynamoEntity e = entity(null, "ann-1");
        AnnotazioneStoricoDynamoEntity saved = repository.save(e);
        assertNotNull(saved.getId());
        verify(table).putItem(e);
    }

    @Test
    void save_generatesAnIdWhenEmpty() {
        AnnotazioneStoricoDynamoEntity e = entity("", "ann-1");
        assertFalse(repository.save(e).getId().isEmpty());
    }

    @Test
    void save_keepsAnExistingId() {
        AnnotazioneStoricoDynamoEntity e = entity("storico-1", "ann-1");
        assertEquals("storico-1", repository.save(e).getId());
    }

    @Test
    void findByIdOriginale_filtersTheScannedEntities() {
        tableContains(entity("s-1", "ann-1"), entity("s-2", "ann-2"), entity("s-3", "ann-1"));
        assertEquals(2, repository.findByIdOriginale("ann-1").size());
    }

    @Test
    void findByIdOriginale_whenNoMatch_returnsEmptyList() {
        tableContains(entity("s-1", "ann-1"));
        assertTrue(repository.findByIdOriginale("ann-999").isEmpty());
    }

    @Test
    void findByIdOriginale_whenTableIsEmpty_returnsEmptyList() {
        tableContains();
        assertTrue(repository.findByIdOriginale("ann-1").isEmpty());
    }
}
