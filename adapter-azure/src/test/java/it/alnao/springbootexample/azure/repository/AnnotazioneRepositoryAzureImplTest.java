package it.alnao.springbootexample.azure.repository;

import it.alnao.springbootexample.azure.entity.AnnotazioneCosmosEntity;
import it.alnao.springbootexample.core.domain.Annotazione;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AnnotazioneRepositoryAzureImplTest {

    @Mock AnnotazioneCosmosRepository cosmosRepository;
    @InjectMocks AnnotazioneRepositoryAzureImpl repository;

    private UUID testId;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        testId = UUID.randomUUID();
    }

    @Test
    void save_convertsAndSaves() {
        AnnotazioneCosmosEntity entity = buildEntity(testId);
        when(cosmosRepository.save(any())).thenReturn(entity);
        Annotazione result = repository.save(new Annotazione(testId, "1.0", "nota"));
        assertNotNull(result);
        verify(cosmosRepository).save(any());
    }

    @Test
    void findById_whenFound_returnsDomain() {
        when(cosmosRepository.findById(testId.toString())).thenReturn(Optional.of(buildEntity(testId)));
        Optional<Annotazione> result = repository.findById(testId);
        assertTrue(result.isPresent());
    }

    @Test
    void findById_whenNotFound_returnsEmpty() {
        when(cosmosRepository.findById(anyString())).thenReturn(Optional.empty());
        assertTrue(repository.findById(testId).isEmpty());
    }

    @Test
    void findAll_returnsMappedList() {
        List<AnnotazioneCosmosEntity> entities = new ArrayList<>();
        entities.add(buildEntity(testId));
        when(cosmosRepository.findAll()).thenReturn(entities);
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void deleteById_callsRepository() {
        repository.deleteById(testId);
        verify(cosmosRepository).deleteById(testId.toString());
    }

    @Test
    void existsById_delegatesToRepository() {
        when(cosmosRepository.existsById(testId.toString())).thenReturn(true);
        assertTrue(repository.existsById(testId));
    }

    private AnnotazioneCosmosEntity buildEntity(UUID id) {
        AnnotazioneCosmosEntity e = new AnnotazioneCosmosEntity();
        e.setId(id.toString());
        e.setVersioneNota("1.0");
        e.setValoreNota("nota");
        return e;
    }
}
