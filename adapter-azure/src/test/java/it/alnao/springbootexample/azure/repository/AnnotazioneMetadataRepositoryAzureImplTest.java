package it.alnao.springbootexample.azure.repository;

import it.alnao.springbootexample.azure.entity.AnnotazioneMetadataSqlServerEntity;
import it.alnao.springbootexample.core.domain.AnnotazioneMetadata;
import it.alnao.springbootexample.core.domain.StatoAnnotazione;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AnnotazioneMetadataRepositoryAzureImplTest {

    @Mock AnnotazioneMetadataSqlServerRepository sqlRepository;
    @InjectMocks AnnotazioneMetadataRepositoryAzureImpl repository;

    private UUID testId;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        testId = UUID.randomUUID();
    }

    @Test
    void save_convertsAndSaves() {
        AnnotazioneMetadataSqlServerEntity entity = buildEntity(testId);
        when(sqlRepository.save(any())).thenReturn(entity);
        AnnotazioneMetadata result = repository.save(buildMetadata(testId));
        assertNotNull(result);
        verify(sqlRepository).save(any());
    }

    @Test
    void findById_whenFound_returnsDomain() {
        when(sqlRepository.findById(testId.toString())).thenReturn(Optional.of(buildEntity(testId)));
        Optional<AnnotazioneMetadata> result = repository.findById(testId);
        assertTrue(result.isPresent());
    }

    @Test
    void findById_whenNotFound_returnsEmpty() {
        when(sqlRepository.findById(anyString())).thenReturn(Optional.empty());
        assertTrue(repository.findById(testId).isEmpty());
    }

    @Test
    void findAll_returnsMappedList() {
        when(sqlRepository.findAll()).thenReturn(List.of(buildEntity(testId)));
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void deleteById_callsRepository() {
        repository.deleteById(testId);
        verify(sqlRepository).deleteById(testId.toString());
    }

    @Test
    void existsById_delegatesToRepository() {
        when(sqlRepository.existsById(testId.toString())).thenReturn(true);
        assertTrue(repository.existsById(testId));
    }

    @Test
    void findByDescrizioneContaining_returnsMappedList() {
        when(sqlRepository.findByDescrizioneContainingIgnoreCase("test")).thenReturn(List.of(buildEntity(testId)));
        assertEquals(1, repository.findByDescrizioneContaining("test").size());
    }

    @Test
    void findByStato_returnsMappedList() {
        when(sqlRepository.findByStato("INSERITA")).thenReturn(List.of(buildEntity(testId)));
        assertEquals(1, repository.findByStato(StatoAnnotazione.INSERITA).size());
    }

    @Test
    void findByTagsContaining_returnsEmptyList() {
        assertTrue(repository.findByTagsContaining("tag").isEmpty());
    }

    @Test
    void count_delegatesToRepository() {
        when(sqlRepository.count()).thenReturn(5L);
        assertEquals(5L, repository.count());
    }

    @Test
    void countByUtenteCreazione_delegatesToRepository() {
        when(sqlRepository.countByUtenteCreazione("user")).thenReturn(3L);
        assertEquals(3L, repository.countByUtenteCreazione("user"));
    }

    @Test
    void findByCategoria_returnsMappedList() {
        when(sqlRepository.findByCategoria("cat")).thenReturn(List.of(buildEntity(testId)));
        assertEquals(1, repository.findByCategoria("cat").size());
    }

    @Test
    void findByPubblica_returnsMappedList() {
        when(sqlRepository.findByPubblica(true)).thenReturn(List.of(buildEntity(testId)));
        assertEquals(1, repository.findByPubblica(true).size());
    }

    @Test
    void findByPriorita_returnsMappedList() {
        when(sqlRepository.findByPriorita(1)).thenReturn(List.of(buildEntity(testId)));
        assertEquals(1, repository.findByPriorita(1).size());
    }

    @Test
    void findByUtenteCreazione_returnsMappedList() {
        when(sqlRepository.findByUtenteCreazione("user")).thenReturn(List.of(buildEntity(testId)));
        assertEquals(1, repository.findByUtenteCreazione("user").size());
    }

    @Test
    void findByDataInserimentoBetween_returnsMappedList() {
        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to = LocalDateTime.now();
        when(sqlRepository.findByDataInserimentoBetween(from, to)).thenReturn(List.of(buildEntity(testId)));
        assertEquals(1, repository.findByDataInserimentoBetween(from, to).size());
    }

    private AnnotazioneMetadata buildMetadata(UUID id) {
        AnnotazioneMetadata m = new AnnotazioneMetadata();
        m.setId(id);
        m.setVersioneNota("1.0");
        m.setUtenteCreazione("user");
        m.setDescrizione("desc");
        m.setStato("INSERITA");
        m.setDataInserimento(LocalDateTime.now());
        m.setDataUltimaModifica(LocalDateTime.now());
        return m;
    }

    private AnnotazioneMetadataSqlServerEntity buildEntity(UUID id) {
        AnnotazioneMetadataSqlServerEntity e = new AnnotazioneMetadataSqlServerEntity();
        e.setId(id.toString());
        e.setVersioneNota("1.0");
        e.setUtenteCreazione("user");
        e.setDescrizione("desc");
        e.setStato("INSERITA");
        e.setDataInserimento(LocalDateTime.now());
        e.setDataUltimaModifica(LocalDateTime.now());
        return e;
    }
}
