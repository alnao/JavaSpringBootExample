package it.alnao.springbootexample.aws.service;

import it.alnao.springbootexample.aws.entity.AnnotazioneStoricoDynamoEntity;
import it.alnao.springbootexample.aws.repository.AnnotazioneStoricoDynamoRepository;
import it.alnao.springbootexample.port.domain.Annotazione;
import it.alnao.springbootexample.port.domain.AnnotazioneCompleta;
import it.alnao.springbootexample.port.domain.AnnotazioneMetadata;
import it.alnao.springbootexample.port.repository.AnnotazioneRepository;
import it.alnao.springbootexample.port.repository.AnnotazioneMetadataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AnnotazioneServiceAwsImplTest {
    @Mock AnnotazioneRepository annotazioneRepository;
    @Mock AnnotazioneMetadataRepository metadataRepository;
    @Mock AnnotazioneStoricoDynamoRepository storicoDynamoRepository;
    @InjectMocks AnnotazioneServiceAwsImpl service;

    @BeforeEach
    void setUp() { MockitoAnnotations.openMocks(this); }

    @Test
    void aggiornaAnnotazione_salvaStoricoPrimaDiAggiornare() {
        UUID id = UUID.randomUUID();
        Annotazione nota = new Annotazione();
        nota.setId(id);
        nota.setVersioneNota("v1.0");
        nota.setValoreNota("old");
        AnnotazioneMetadata meta = new AnnotazioneMetadata();
        meta.setId(id);
        meta.setDescrizione("desc old");
        meta.setVersioneNota("v1.0");
        meta.setUtenteUltimaModifica("utente");
        meta.setCategoria("cat");
        meta.setTags("tag");
        meta.setPubblica(true);
        meta.setPriorita(1);
        meta.setDataUltimaModifica(LocalDateTime.now().minusDays(1));

        when(annotazioneRepository.findById(id)).thenReturn(Optional.of(nota));
        when(metadataRepository.findById(id)).thenReturn(Optional.of(meta));
        when(annotazioneRepository.save(any())).thenReturn(nota);
        when(metadataRepository.save(any())).thenReturn(meta);

        service.aggiornaAnnotazione(id, "new", "desc new", "utente2");

        ArgumentCaptor<AnnotazioneStoricoDynamoEntity> storicoCaptor = ArgumentCaptor.forClass(AnnotazioneStoricoDynamoEntity.class);
        verify(storicoDynamoRepository).save(storicoCaptor.capture());
        AnnotazioneStoricoDynamoEntity storico = storicoCaptor.getValue();
        assertEquals(id.toString(), storico.getIdOriginale());
        assertEquals("v1.0", storico.getVersioneNota());
        assertEquals("old", storico.getValoreNota());
        assertEquals("desc old", storico.getDescrizione());
        assertEquals("utente", storico.getUtente());
    }
}
