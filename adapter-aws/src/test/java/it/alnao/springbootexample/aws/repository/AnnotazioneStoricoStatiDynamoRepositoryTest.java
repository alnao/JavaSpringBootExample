package it.alnao.springbootexample.aws.repository;

import it.alnao.springbootexample.aws.entity.AnnotazioneStoricoStatiDynamoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class AnnotazioneStoricoStatiDynamoRepositoryTest {

    @Mock DynamoDbEnhancedClient enhancedClient;
    @Mock DynamoDbTable<AnnotazioneStoricoStatiDynamoEntity> table;
    @Mock PageIterable<AnnotazioneStoricoStatiDynamoEntity> pageIterable;

    private AnnotazioneStoricoStatiDynamoRepository repository;
    private AnnotazioneStoricoStatiDynamoEntity entity;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        doReturn(table).when(enhancedClient).table(eq("annotazioni_storicoStati"), any());
        repository = new AnnotazioneStoricoStatiDynamoRepository(enhancedClient);
        entity = new AnnotazioneStoricoStatiDynamoEntity(
                "op-1", "ann-1", "1.0", "INVIATA", "DAINVIARE",
                "mario", "2026-02-02T10:00:00", "nota");
    }

    private void tableContains(AnnotazioneStoricoStatiDynamoEntity... entities) {
        SdkIterable<AnnotazioneStoricoStatiDynamoEntity> items = () -> List.of(entities).iterator();
        doReturn(items).when(pageIterable).items();
        doReturn(pageIterable).when(table).scan();
    }

    @Test
    void save_putsTheItemAndReturnsIt() {
        assertSame(entity, repository.save(entity));
        verify(table).putItem(entity);
    }

    @Test
    void findById_whenPresent_wrapsTheResult() {
        doReturn(entity).when(table).getItem(any(Consumer.class));
        Optional<AnnotazioneStoricoStatiDynamoEntity> result = repository.findById("op-1");
        assertTrue(result.isPresent());
        assertEquals("op-1", result.get().getIdOperazione());
    }

    @Test
    void findById_whenAbsent_returnsEmpty() {
        doReturn(null).when(table).getItem(any(Consumer.class));
        assertTrue(repository.findById("op-999").isEmpty());
    }

    /**
     * findByIdAnnotazione non e' ancora implementata (manca un GSI su idAnnotazione)
     * e restituisce sempre una lista vuota: il test fissa il comportamento attuale.
     */
    @Test
    void findByIdAnnotazione_nonImplementata_ritornaSempreListaVuota() {
        assertTrue(repository.findByIdAnnotazione("ann-1").isEmpty());
        verify(table, never()).scan();
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
        repository.deleteById("op-1");
        verify(table).deleteItem(any(Consumer.class));
    }

    @Test
    void existsById_reflectsFindById() {
        doReturn(entity).when(table).getItem(any(Consumer.class));
        assertTrue(repository.existsById("op-1"));
        doReturn(null).when(table).getItem(any(Consumer.class));
        assertFalse(repository.existsById("op-1"));
    }

    @Test
    void count_countsTheScannedItems() {
        tableContains(entity, entity, entity);
        assertEquals(3L, repository.count());
    }
}
