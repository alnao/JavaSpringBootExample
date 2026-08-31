package it.alnao.springbootexample.aws.repository;

import it.alnao.springbootexample.aws.entity.AnnotazioneDynamoEntity;
import it.alnao.springbootexample.core.domain.Annotazione;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AnnotazioneRepositoryAwsImplTest {

    @Mock AnnotazioneDynamoRepository dynamoRepository;
    @InjectMocks AnnotazioneRepositoryAwsImpl repository;

    private UUID id;
    private AnnotazioneDynamoEntity entity;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        id = UUID.randomUUID();
        entity = new AnnotazioneDynamoEntity(id.toString(), "1.0", "il testo della nota");
    }

    @Test
    void save_mapsDomainToEntityAndBack() {
        Annotazione domain = new Annotazione(id, "1.0", "il testo della nota");
        when(dynamoRepository.save(any(AnnotazioneDynamoEntity.class))).thenReturn(entity);

        Annotazione saved = repository.save(domain);

        assertEquals(id, saved.getId());
        assertEquals("1.0", saved.getVersioneNota());
        assertEquals("il testo della nota", saved.getValoreNota());
        verify(dynamoRepository).save(any(AnnotazioneDynamoEntity.class));
    }

    @Test
    void findById_convertsUuidToStringKey() {
        when(dynamoRepository.findById(id.toString())).thenReturn(Optional.of(entity));
        Optional<Annotazione> result = repository.findById(id);
        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
    }

    @Test
    void findById_whenMissing_returnsEmpty() {
        when(dynamoRepository.findById(id.toString())).thenReturn(Optional.empty());
        assertTrue(repository.findById(id).isEmpty());
    }

    @Test
    void findAll_mapsEveryEntity() {
        when(dynamoRepository.findAll()).thenReturn(List.of(entity, entity));
        assertEquals(2, repository.findAll().size());
    }

    @Test
    void deleteById_convertsUuidToStringKey() {
        repository.deleteById(id);
        verify(dynamoRepository).deleteById(id.toString());
    }

    @Test
    void existsById_convertsUuidToStringKey() {
        when(dynamoRepository.existsById(id.toString())).thenReturn(true);
        assertTrue(repository.existsById(id));
    }

    @Test
    void findByValoreNotaContaining_delegatesAndMaps() {
        when(dynamoRepository.findByValoreNotaContaining("testo")).thenReturn(List.of(entity));
        List<Annotazione> result = repository.findByValoreNotaContaining("testo");
        assertEquals(1, result.size());
        assertEquals("il testo della nota", result.get(0).getValoreNota());
    }

    @Test
    void findByValoreNotaContaining_whenNoMatch_returnsEmptyList() {
        when(dynamoRepository.findByValoreNotaContaining("assente")).thenReturn(List.of());
        assertTrue(repository.findByValoreNotaContaining("assente").isEmpty());
    }

    @Test
    void findByVersioneNota_delegatesAndMaps() {
        when(dynamoRepository.findByVersioneNota("1.0")).thenReturn(List.of(entity));
        assertEquals("1.0", repository.findByVersioneNota("1.0").get(0).getVersioneNota());
    }

    @Test
    void count_delegates() {
        when(dynamoRepository.count()).thenReturn(6L);
        assertEquals(6L, repository.count());
    }
}
