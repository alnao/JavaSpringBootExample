package it.alnao.springbootexample.mongodb.repository;

import it.alnao.springbootexample.mongodb.entity.AnnotazioneEntity;
import it.alnao.springbootexample.core.domain.Annotazione;
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
class AnnotazioneRepositoryImplTest {

    @Mock
    private AnnotazioneMongoRepository mongoRepository;

    @InjectMocks
    private AnnotazioneRepositoryImpl repository;

    private UUID testId;
    private AnnotazioneEntity entity;
    private Annotazione domain;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        
        entity = new AnnotazioneEntity();
        entity.setId(testId.toString());
        entity.setVersioneNota("v1.0");
        entity.setValoreNota("Test valore");
        
        domain = new Annotazione();
        domain.setId(testId);
        domain.setVersioneNota("v1.0");
        domain.setValoreNota("Test valore");
    }

    @Test
    void testSave() {
        when(mongoRepository.save(any(AnnotazioneEntity.class))).thenReturn(entity);
        
        Annotazione result = repository.save(domain);
        
        assertNotNull(result);
        assertEquals(testId, result.getId());
        assertEquals("v1.0", result.getVersioneNota());
        verify(mongoRepository, times(1)).save(any(AnnotazioneEntity.class));
    }

    @Test
    void testFindById() {
        when(mongoRepository.findById(testId.toString())).thenReturn(Optional.of(entity));
        
        Optional<Annotazione> result = repository.findById(testId);
        
        assertTrue(result.isPresent());
        assertEquals(testId, result.get().getId());
        verify(mongoRepository, times(1)).findById(testId.toString());
    }

    @Test
    void testFindAll() {
        when(mongoRepository.findAll()).thenReturn(Arrays.asList(entity));
        
        List<Annotazione> result = repository.findAll();
        
        assertEquals(1, result.size());
        assertEquals(testId, result.get(0).getId());
        verify(mongoRepository, times(1)).findAll();
    }

    @Test
    void testDeleteById() {
        doNothing().when(mongoRepository).deleteById(testId.toString());
        
        repository.deleteById(testId);
        
        verify(mongoRepository, times(1)).deleteById(testId.toString());
    }

    @Test
    void testExistsById() {
        when(mongoRepository.existsById(testId.toString())).thenReturn(true);
        
        boolean result = repository.existsById(testId);
        
        assertTrue(result);
        verify(mongoRepository, times(1)).existsById(testId.toString());
    }

    @Test
    void testCount() {
        when(mongoRepository.count()).thenReturn(5L);
        
        long result = repository.count();
        
        assertEquals(5L, result);
        verify(mongoRepository, times(1)).count();
    }
}
