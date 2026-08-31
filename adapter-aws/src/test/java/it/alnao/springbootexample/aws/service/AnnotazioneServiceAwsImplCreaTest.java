package it.alnao.springbootexample.aws.service;

import it.alnao.springbootexample.aws.entity.AnnotazioneStoricoDynamoEntity;
import it.alnao.springbootexample.aws.repository.AnnotazioneStoricoDynamoRepository;
import it.alnao.springbootexample.core.domain.Annotazione;
import it.alnao.springbootexample.core.domain.AnnotazioneCompleta;
import it.alnao.springbootexample.core.domain.AnnotazioneMetadata;
import it.alnao.springbootexample.core.domain.StatoAnnotazione;
import it.alnao.springbootexample.core.repository.AnnotazioneMetadataRepository;
import it.alnao.springbootexample.core.repository.AnnotazioneRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Copre creazione e i rami di aggiornamento non verificati in
 * {@link AnnotazioneServiceAwsImplTest}.
 */
class AnnotazioneServiceAwsImplCreaTest {

    @Mock AnnotazioneRepository annotazioneRepository;
    @Mock AnnotazioneMetadataRepository metadataRepository;
    @Mock AnnotazioneStoricoDynamoRepository storicoDynamoRepository;
    @InjectMocks AnnotazioneServiceAwsImpl service;

    private UUID id;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        id = UUID.randomUUID();
    }

    private void esistente(String valore, String versione) {
        Annotazione ann = new Annotazione(id, versione, valore);
        AnnotazioneMetadata meta = new AnnotazioneMetadata(id, versione, "mario", "descrizione");
        meta.setDataUltimaModifica(LocalDateTime.now().minusDays(1));
        meta.setUtenteUltimaModifica("mario");
        when(annotazioneRepository.findById(id)).thenReturn(Optional.of(ann));
        when(metadataRepository.findById(id)).thenReturn(Optional.of(meta));
        when(annotazioneRepository.save(any(Annotazione.class))).thenAnswer(i -> i.getArgument(0));
        when(metadataRepository.save(any(AnnotazioneMetadata.class))).thenAnswer(i -> i.getArgument(0));
    }

    // ---------- creaAnnotazione ----------

    @Test
    void creaAnnotazione_savesBothPartsWithStatoInserita() {
        when(annotazioneRepository.save(any(Annotazione.class))).thenAnswer(i -> i.getArgument(0));
        when(metadataRepository.save(any(AnnotazioneMetadata.class))).thenAnswer(i -> i.getArgument(0));

        AnnotazioneCompleta result = service.creaAnnotazione("il testo", "la descrizione", "mario");

        assertEquals("il testo", result.getAnnotazione().getValoreNota());
        assertEquals("1.0", result.getAnnotazione().getVersioneNota());
        assertEquals("la descrizione", result.getMetadata().getDescrizione());
        assertEquals("mario", result.getMetadata().getUtenteCreazione());
        assertEquals(StatoAnnotazione.INSERITA.getValue(), result.getMetadata().getStato());
        assertEquals(result.getAnnotazione().getId(), result.getMetadata().getId());
    }

    @Test
    void creaAnnotazione_stampsTheCreationDates() {
        when(annotazioneRepository.save(any(Annotazione.class))).thenAnswer(i -> i.getArgument(0));
        when(metadataRepository.save(any(AnnotazioneMetadata.class))).thenAnswer(i -> i.getArgument(0));

        AnnotazioneCompleta result = service.creaAnnotazione("testo", "descr", "mario");

        assertNotNull(result.getMetadata().getDataInserimento());
        assertNotNull(result.getMetadata().getDataUltimaModifica());
        assertEquals("mario", result.getMetadata().getUtenteUltimaModifica());
    }

    // ---------- aggiornaAnnotazione ----------

    @Test
    void aggiornaAnnotazione_archivesTheOldVersionBeforeUpdating() {
        esistente("vecchio testo", "1.0");

        service.aggiornaAnnotazione(id, "nuovo testo", "nuova descrizione", "luigi");

        ArgumentCaptor<AnnotazioneStoricoDynamoEntity> captor =
                ArgumentCaptor.forClass(AnnotazioneStoricoDynamoEntity.class);
        verify(storicoDynamoRepository).save(captor.capture());
        assertEquals(id.toString(), captor.getValue().getIdOriginale());
        assertEquals("vecchio testo", captor.getValue().getValoreNota());
        assertEquals("1.0", captor.getValue().getVersioneNota());
    }

    @Test
    void aggiornaAnnotazione_incrementsTheVersionWhenTheTextChanges() {
        esistente("vecchio testo", "1.0");

        AnnotazioneCompleta result = service.aggiornaAnnotazione(id, "nuovo testo", "nuova descr", "luigi");

        assertEquals("nuovo testo", result.getAnnotazione().getValoreNota());
        assertNotEquals("1.0", result.getAnnotazione().getVersioneNota());
        assertEquals(result.getAnnotazione().getVersioneNota(), result.getMetadata().getVersioneNota());
    }

    @Test
    void aggiornaAnnotazione_withNullValue_keepsTextAndVersion() {
        esistente("vecchio testo", "1.0");

        AnnotazioneCompleta result = service.aggiornaAnnotazione(id, null, "solo descrizione", "luigi");

        assertEquals("vecchio testo", result.getAnnotazione().getValoreNota());
        assertEquals("1.0", result.getAnnotazione().getVersioneNota());
        assertEquals("solo descrizione", result.getMetadata().getDescrizione());
    }

    @Test
    void aggiornaAnnotazione_withNullDescription_keepsTheOldOne() {
        esistente("vecchio testo", "1.0");

        AnnotazioneCompleta result = service.aggiornaAnnotazione(id, "nuovo testo", null, "luigi");

        assertEquals("descrizione", result.getMetadata().getDescrizione());
        assertEquals("luigi", result.getMetadata().getUtenteUltimaModifica());
    }

    @Test
    void aggiornaAnnotazione_whenAnnotazioneIsMissing_throws() {
        when(annotazioneRepository.findById(id)).thenReturn(Optional.empty());
        when(metadataRepository.findById(id)).thenReturn(Optional.of(
                new AnnotazioneMetadata(id, "1.0", "mario", "descr")));

        assertThrows(RuntimeException.class,
                () -> service.aggiornaAnnotazione(id, "nuovo", "descr", "luigi"));
        verifyNoInteractions(storicoDynamoRepository);
    }

    @Test
    void aggiornaAnnotazione_whenMetadataIsMissing_throws() {
        when(annotazioneRepository.findById(id)).thenReturn(Optional.of(new Annotazione(id, "1.0", "testo")));
        when(metadataRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> service.aggiornaAnnotazione(id, "nuovo", "descr", "luigi"));
    }
}
