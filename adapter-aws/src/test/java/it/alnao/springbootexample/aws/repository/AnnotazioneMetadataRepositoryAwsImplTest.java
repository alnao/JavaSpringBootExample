package it.alnao.springbootexample.aws.repository;

import it.alnao.springbootexample.aws.entity.AnnotazioneMetadataMysqlEntity;
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

class AnnotazioneMetadataRepositoryAwsImplTest {

    @Mock AnnotazioneMetadataMysqlRepository mysqlRepository;
    @InjectMocks AnnotazioneMetadataRepositoryAwsImpl repository;

    private UUID testId;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        testId = UUID.randomUUID();
    }

    @Test
    void classLoads() {
        assertNotNull(repository);
    }

    @Test
    void save_convertsAndSaves() {
        AnnotazioneMetadataMysqlEntity entity = buildEntity(testId);
        when(mysqlRepository.save(any())).thenReturn(entity);
        AnnotazioneMetadata result = repository.save(buildMetadata(testId));
        assertNotNull(result);
        verify(mysqlRepository).save(any());
    }

    @Test
    void findById_whenFound_returnsDomain() {
        when(mysqlRepository.findById(testId.toString())).thenReturn(Optional.of(buildEntity(testId)));
        assertTrue(repository.findById(testId).isPresent());
    }

    @Test
    void findById_whenNotFound_returnsEmpty() {
        when(mysqlRepository.findById(anyString())).thenReturn(Optional.empty());
        assertTrue(repository.findById(testId).isEmpty());
    }

    @Test
    void findAll_returnsMappedList() {
        when(mysqlRepository.findAll()).thenReturn(List.of(buildEntity(testId)));
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void deleteById_callsRepository() {
        repository.deleteById(testId);
        verify(mysqlRepository).deleteById(testId.toString());
    }

    @Test
    void existsById_delegatesToRepository() {
        when(mysqlRepository.existsById(testId.toString())).thenReturn(true);
        assertTrue(repository.existsById(testId));
    }

    @Test
    void findByDescrizioneContaining_returnsMappedList() {
        when(mysqlRepository.findByDescrizioneContainingIgnoreCase("test")).thenReturn(List.of(buildEntity(testId)));
        assertEquals(1, repository.findByDescrizioneContaining("test").size());
    }

    @Test
    void findByCategoria_returnsMappedList() {
        when(mysqlRepository.findByCategoria("cat")).thenReturn(List.of(buildEntity(testId)));
        assertEquals(1, repository.findByCategoria("cat").size());
    }

    @Test
    void findByPubblica_returnsMappedList() {
        when(mysqlRepository.findByPubblica(true)).thenReturn(List.of(buildEntity(testId)));
        assertEquals(1, repository.findByPubblica(true).size());
    }

    @Test
    void findByStato_returnsMappedList() {
        when(mysqlRepository.findByStato("INSERITA")).thenReturn(List.of(buildEntity(testId)));
        assertEquals(1, repository.findByStato(StatoAnnotazione.INSERITA).size());
    }

    @Test
    void findByPriorita_returnsMappedList() {
        when(mysqlRepository.findByPriorita(1)).thenReturn(List.of(buildEntity(testId)));
        assertEquals(1, repository.findByPriorita(1).size());
    }

    @Test
    void findByUtenteCreazione_returnsMappedList() {
        when(mysqlRepository.findByUtenteCreazione("user")).thenReturn(List.of(buildEntity(testId)));
        assertEquals(1, repository.findByUtenteCreazione("user").size());
    }

    @Test
    void findByTagsContaining_returnsMappedList() {
        when(mysqlRepository.findByTagsContaining("tag")).thenReturn(List.of(buildEntity(testId)));
        assertEquals(1, repository.findByTagsContaining("tag").size());
    }

    @Test
    void findByDataInserimentoBetween_returnsMappedList() {
        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to = LocalDateTime.now();
        when(mysqlRepository.findByDataInserimentoBetween(from, to)).thenReturn(List.of(buildEntity(testId)));
        assertEquals(1, repository.findByDataInserimentoBetween(from, to).size());
    }

    @Test
    void findWithFilters_delegatesToRepository() {
        when(mysqlRepository.findWithFilters("desc", "cat", true, 1)).thenReturn(List.of(buildEntity(testId)));
        assertEquals(1, repository.findWithFilters("desc", "cat", true, 1).size());
    }

    @Test
    void count_delegatesToRepository() {
        when(mysqlRepository.count()).thenReturn(7L);
        assertEquals(7L, repository.count());
    }

    @Test
    void countByUtenteCreazione_delegatesToRepository() {
        when(mysqlRepository.countByUtenteCreazione("user")).thenReturn(3L);
        assertEquals(3L, repository.countByUtenteCreazione("user"));
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

    private AnnotazioneMetadataMysqlEntity buildEntity(UUID id) {
        AnnotazioneMetadataMysqlEntity e = new AnnotazioneMetadataMysqlEntity();
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
