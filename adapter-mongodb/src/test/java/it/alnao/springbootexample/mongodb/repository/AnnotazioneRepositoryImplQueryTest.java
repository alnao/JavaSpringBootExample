package it.alnao.springbootexample.mongodb.repository;

import it.alnao.springbootexample.core.domain.Annotazione;
import it.alnao.springbootexample.mongodb.entity.AnnotazioneEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Copre le query non verificate in {@link AnnotazioneRepositoryImplTest}.
 */
class AnnotazioneRepositoryImplQueryTest {

    @Mock AnnotazioneMongoRepository mongoRepository;
    @InjectMocks AnnotazioneRepositoryImpl repository;

    private AnnotazioneEntity entity;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        UUID id = UUID.randomUUID();
        entity = new AnnotazioneEntity();
        entity.setId(id.toString());
        entity.setVersioneNota("1.0");
        entity.setValoreNota("il testo della nota");
    }

    @Test
    void findByVersioneNota_delegatesAndMaps() {
        when(mongoRepository.findByVersioneNota("1.0")).thenReturn(List.of(entity));
        List<Annotazione> result = repository.findByVersioneNota("1.0");
        assertEquals(1, result.size());
        assertEquals("1.0", result.get(0).getVersioneNota());
    }

    @Test
    void findByVersioneNota_whenNoRows_returnsEmptyList() {
        when(mongoRepository.findByVersioneNota("9.9")).thenReturn(List.of());
        assertTrue(repository.findByVersioneNota("9.9").isEmpty());
    }

    @Test
    void findByValoreNotaContaining_delegatesAndMaps() {
        when(mongoRepository.findByValoreNotaContainingIgnoreCase("testo")).thenReturn(List.of(entity));
        List<Annotazione> result = repository.findByValoreNotaContaining("testo");
        assertEquals(1, result.size());
        assertEquals("il testo della nota", result.get(0).getValoreNota());
    }

    @Test
    void findByValoreNotaContaining_whenNoMatch_returnsEmptyList() {
        when(mongoRepository.findByValoreNotaContainingIgnoreCase("assente")).thenReturn(List.of());
        assertTrue(repository.findByValoreNotaContaining("assente").isEmpty());
    }
}
