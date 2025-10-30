package it.alnao.springbootexample.postgresql.repository;

import it.alnao.springbootexample.postgresql.entity.AnnotazioneMetadataEntity;
import it.alnao.springbootexample.core.domain.AnnotazioneMetadata;
import it.alnao.springbootexample.core.domain.StatoAnnotazione;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnnotazioneMetadataRepositoryImplTest {

    @Mock
    private AnnotazioneMetadataJpaRepository jpaRepository;

    @InjectMocks
    private AnnotazioneMetadataRepositoryImpl repository;

    private UUID testId;
    private AnnotazioneMetadataEntity entity;
    private AnnotazioneMetadata domain;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        
        entity = new AnnotazioneMetadataEntity();
        entity.setId(testId.toString());
        entity.setVersioneNota("v1.0");
        entity.setDescrizione("Test descrizione");
        entity.setStato("INSERITA");
        
        domain = new AnnotazioneMetadata();
        domain.setId(testId);
        domain.setVersioneNota("v1.0");
        domain.setDescrizione("Test descrizione");
        domain.setStato("INSERITA");
    }

    @Test
    void testSave() {
        when(jpaRepository.save(any(AnnotazioneMetadataEntity.class))).thenReturn(entity);
        
        AnnotazioneMetadata result = repository.save(domain);
        
        assertNotNull(result);
        assertEquals(testId, result.getId());
        assertEquals("v1.0", result.getVersioneNota());
        verify(jpaRepository, times(1)).save(any(AnnotazioneMetadataEntity.class));
    }

    @Test
    void testFindById() {
        when(jpaRepository.findById(testId.toString())).thenReturn(Optional.of(entity));
        
        Optional<AnnotazioneMetadata> result = repository.findById(testId);
        
        assertTrue(result.isPresent());
        assertEquals(testId, result.get().getId());
        verify(jpaRepository, times(1)).findById(testId.toString());
    }

    @Test
    void testFindAll() {
        when(jpaRepository.findAll()).thenReturn(Arrays.asList(entity));
        
        List<AnnotazioneMetadata> result = repository.findAll();
        
        assertEquals(1, result.size());
        assertEquals(testId, result.get(0).getId());
        verify(jpaRepository, times(1)).findAll();
    }

    @Test
    void testDeleteById() {
        doNothing().when(jpaRepository).deleteById(testId.toString());
        
        repository.deleteById(testId);
        
        verify(jpaRepository, times(1)).deleteById(testId.toString());
    }

    @Test
    void testExistsById() {
        when(jpaRepository.existsById(testId.toString())).thenReturn(true);
        
        boolean result = repository.existsById(testId);
        
        assertTrue(result);
        verify(jpaRepository, times(1)).existsById(testId.toString());
    }

    @Test
    void testCount() {
        when(jpaRepository.count()).thenReturn(10L);
        
        long result = repository.count();
        
        assertEquals(10L, result);
        verify(jpaRepository, times(1)).count();
    }

    @Test
    void testFindByStato() {
        when(jpaRepository.findByStato("INSERITA")).thenReturn(Arrays.asList(entity));
        
        List<AnnotazioneMetadata> result = repository.findByStato(StatoAnnotazione.INSERITA);
        
        assertEquals(1, result.size());
        verify(jpaRepository, times(1)).findByStato("INSERITA");
    }
}
