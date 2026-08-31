package it.alnao.springbootexample.aws.repository;

import it.alnao.springbootexample.aws.entity.AnnotazioneDynamoEntity;
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
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class AnnotazioneDynamoRepositoryTest {

    @Mock DynamoDbEnhancedClient enhancedClient;
    @Mock DynamoDbTable<AnnotazioneDynamoEntity> table;
    @Mock PageIterable<AnnotazioneDynamoEntity> pageIterable;

    private AnnotazioneDynamoRepository repository;
    private AnnotazioneDynamoEntity entity;
    private String id;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        NoSqlTableConfig tableConfig = new NoSqlTableConfig();
        tableConfig.setAnnotazioniTableName("annotazioni-test");
        doReturn(table).when(enhancedClient).table(eq("annotazioni-test"), any());
        repository = new AnnotazioneDynamoRepository(enhancedClient, tableConfig);

        id = UUID.randomUUID().toString();
        entity = new AnnotazioneDynamoEntity(id, "1.0", "Il Testo Della Nota");
    }

    /** table.scan(...) ritorna una PageIterable i cui items() alimentano findAll(). */
    private void tableContains(AnnotazioneDynamoEntity... entities) {
        SdkIterable<AnnotazioneDynamoEntity> items = () -> List.of(entities).iterator();
        doReturn(items).when(pageIterable).items();
        doReturn(pageIterable).when(table).scan(any(ScanEnhancedRequest.class));
    }

    @Test
    void save_putsTheItemAndReturnsIt() {
        AnnotazioneDynamoEntity saved = repository.save(entity);
        assertSame(entity, saved);
        verify(table).putItem(entity);
    }

    @Test
    void findById_whenPresent_wrapsTheResult() {
        doReturn(entity).when(table).getItem(any(Consumer.class));
        Optional<AnnotazioneDynamoEntity> result = repository.findById(id);
        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
    }

    @Test
    void findById_whenAbsent_returnsEmpty() {
        doReturn(null).when(table).getItem(any(Consumer.class));
        assertTrue(repository.findById(id).isEmpty());
    }

    @Test
    void findAll_collectsEveryScannedItem() {
        tableContains(entity, entity);
        assertEquals(2, repository.findAll().size());
    }

    @Test
    void findAll_whenTableIsEmpty_returnsEmptyList() {
        tableContains();
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void deleteById_delegatesToTheTable() {
        repository.deleteById(id);
        verify(table).deleteItem(any(Consumer.class));
    }

    @Test
    void existsById_reflectsFindById() {
        doReturn(entity).when(table).getItem(any(Consumer.class));
        assertTrue(repository.existsById(id));
        doReturn(null).when(table).getItem(any(Consumer.class));
        assertFalse(repository.existsById(id));
    }

    @Test
    void findByValoreNotaContaining_isCaseInsensitive() {
        tableContains(entity);
        assertEquals(1, repository.findByValoreNotaContaining("testo").size());
        tableContains(entity);
        assertEquals(1, repository.findByValoreNotaContaining("TESTO").size());
    }

    @Test
    void findByValoreNotaContaining_whenNoMatch_returnsEmptyList() {
        tableContains(entity);
        assertTrue(repository.findByValoreNotaContaining("assente").isEmpty());
    }

    @Test
    void findByValoreNotaContaining_skipsRowsWithoutText() {
        AnnotazioneDynamoEntity senzaTesto = new AnnotazioneDynamoEntity(id, "1.0", null);
        tableContains(senzaTesto);
        assertTrue(repository.findByValoreNotaContaining("testo").isEmpty());
    }

    @Test
    void findByVersioneNota_filtersOnTheExactVersion() {
        AnnotazioneDynamoEntity altraVersione =
                new AnnotazioneDynamoEntity(UUID.randomUUID().toString(), "2.0", "altro");
        tableContains(entity, altraVersione);
        assertEquals(1, repository.findByVersioneNota("1.0").size());
    }

    @Test
    void count_countsTheScannedItems() {
        tableContains(entity, entity, entity);
        assertEquals(3L, repository.count());
    }
}
