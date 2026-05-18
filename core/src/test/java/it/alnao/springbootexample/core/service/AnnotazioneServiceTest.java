package it.alnao.springbootexample.core.service;

import it.alnao.springbootexample.core.domain.Annotazione;
import it.alnao.springbootexample.core.domain.AnnotazioneCompleta;
import it.alnao.springbootexample.core.domain.AnnotazioneMetadata;
import it.alnao.springbootexample.core.domain.StatoAnnotazione;
import it.alnao.springbootexample.core.repository.AnnotazioneMetadataRepository;
import it.alnao.springbootexample.core.repository.AnnotazioneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the AbstractAnnotazioneService by using a concrete stub that delegates
 * to mock repositories.
 */
class AnnotazioneServiceTest {

    private AnnotazioneRepository mockAnnotazioneRepo;
    private AnnotazioneMetadataRepository mockMetadataRepo;
    private AbstractAnnotazioneService service;

    // Minimal concrete subclass for testing
    static class StubAnnotazioneService extends AbstractAnnotazioneService {
        private final AnnotazioneRepository annotazioneRepository;
        private final AnnotazioneMetadataRepository metadataRepository;

        StubAnnotazioneService(AnnotazioneRepository annotazioneRepository,
                               AnnotazioneMetadataRepository metadataRepository) {
            this.annotazioneRepository = annotazioneRepository;
            this.metadataRepository = metadataRepository;
        }

        @Override
        protected AnnotazioneRepository getAnnotazioneRepository() { return annotazioneRepository; }

        @Override
        protected AnnotazioneMetadataRepository getMetadataRepository() { return metadataRepository; }

        @Override
        public AnnotazioneCompleta creaAnnotazione(String valoreNota, String descrizione, String utente) {
            throw new UnsupportedOperationException("stub");
        }

        @Override
        public AnnotazioneCompleta aggiornaAnnotazione(UUID id, String nuovoValore, String nuovaDescrizione, String utente) {
            throw new UnsupportedOperationException("stub");
        }
    }

    private UUID testId;
    private Annotazione testAnnotazione;
    private AnnotazioneMetadata testMetadata;

    @BeforeEach
    void setUp() {
        mockAnnotazioneRepo = Mockito.mock(AnnotazioneRepository.class);
        mockMetadataRepo = Mockito.mock(AnnotazioneMetadataRepository.class);
        service = new StubAnnotazioneService(mockAnnotazioneRepo, mockMetadataRepo);

        testId = UUID.randomUUID();
        testAnnotazione = new Annotazione();
        testAnnotazione.setId(testId);
        testAnnotazione.setVersioneNota("1.0");
        testAnnotazione.setValoreNota("valore test");

        testMetadata = new AnnotazioneMetadata();
        testMetadata.setId(testId);
        testMetadata.setVersioneNota("1.0");
        testMetadata.setUtenteCreazione("user1");
        testMetadata.setDataInserimento(LocalDateTime.now());
        testMetadata.setDataUltimaModifica(LocalDateTime.now());
        testMetadata.setStato(StatoAnnotazione.INSERITA.getValue());
        testMetadata.setCategoria("Default");
        testMetadata.setTags("");
        testMetadata.setPubblica(false);
        testMetadata.setPriorita(1);
    }

    // --- cambiaStato ---

    @Test
    void cambiaStato_statoValido_aggiorna() {
        when(mockMetadataRepo.findById(testId)).thenReturn(Optional.of(testMetadata));
        when(mockAnnotazioneRepo.findById(testId)).thenReturn(Optional.of(testAnnotazione));
        when(mockMetadataRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AnnotazioneCompleta result = service.cambiaStato(testId, StatoAnnotazione.MODIFICATA.getValue(), "user1");

        assertNotNull(result);
        assertEquals(StatoAnnotazione.MODIFICATA.getValue(), result.getMetadata().getStato());
    }

    @Test
    void cambiaStato_statoNonValido_lancia() {
        when(mockMetadataRepo.findById(testId)).thenReturn(Optional.of(testMetadata));
        when(mockAnnotazioneRepo.findById(testId)).thenReturn(Optional.of(testAnnotazione));

        assertThrows(IllegalArgumentException.class,
                () -> service.cambiaStato(testId, "STATO_INVALIDO", "user1"));
    }

    @Test
    void cambiaStato_annotazioneNonTrovata_lancia() {
        when(mockMetadataRepo.findById(testId)).thenReturn(Optional.empty());
        when(mockAnnotazioneRepo.findById(testId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> service.cambiaStato(testId, StatoAnnotazione.INSERITA.getValue(), "user1"));
    }

    // --- trovaPerID ---

    @Test
    void trovaPerID_trovata_ritornaPresent() {
        when(mockAnnotazioneRepo.findById(testId)).thenReturn(Optional.of(testAnnotazione));
        when(mockMetadataRepo.findById(testId)).thenReturn(Optional.of(testMetadata));

        Optional<AnnotazioneCompleta> result = service.trovaPerID(testId);

        assertTrue(result.isPresent());
        assertEquals(testId, result.get().getAnnotazione().getId());
    }

    @Test
    void trovaPerID_nonTrovata_ritornaEmpty() {
        when(mockAnnotazioneRepo.findById(testId)).thenReturn(Optional.empty());
        when(mockMetadataRepo.findById(testId)).thenReturn(Optional.empty());

        assertTrue(service.trovaPerID(testId).isEmpty());
    }

    // --- trovaTutte ---

    @Test
    void trovaTutte_ritornaLista() {
        when(mockAnnotazioneRepo.findAll()).thenReturn(List.of(testAnnotazione));
        when(mockMetadataRepo.findAll()).thenReturn(List.of(testMetadata));

        List<AnnotazioneCompleta> result = service.trovaTutte();

        assertEquals(1, result.size());
    }

    @Test
    void trovaTutte_senzaMetadata_esclusa() {
        Annotazione altra = new Annotazione();
        altra.setId(UUID.randomUUID());
        when(mockAnnotazioneRepo.findAll()).thenReturn(List.of(testAnnotazione, altra));
        when(mockMetadataRepo.findAll()).thenReturn(List.of(testMetadata)); // solo per testId

        List<AnnotazioneCompleta> result = service.trovaTutte();
        assertEquals(1, result.size());
    }

    // --- trovaPerUtente ---

    @Test
    void trovaPerUtente_ritornaLista() {
        when(mockMetadataRepo.findByUtenteCreazione("user1")).thenReturn(List.of(testMetadata));
        when(mockAnnotazioneRepo.findById(testId)).thenReturn(Optional.of(testAnnotazione));

        List<AnnotazioneCompleta> result = service.trovaPerUtente("user1");
        assertEquals(1, result.size());
    }

    // --- trovaPerCategoria ---

    @Test
    void trovaPerCategoria_ritornaLista() {
        when(mockMetadataRepo.findByCategoria("Default")).thenReturn(List.of(testMetadata));
        when(mockAnnotazioneRepo.findById(testId)).thenReturn(Optional.of(testAnnotazione));

        assertEquals(1, service.trovaPerCategoria("Default").size());
    }

    // --- trovaPerPeriodo ---

    @Test
    void trovaPerPeriodo_ritornaLista() {
        LocalDateTime inizio = LocalDateTime.now().minusDays(1);
        LocalDateTime fine = LocalDateTime.now().plusDays(1);
        when(mockMetadataRepo.findByDataInserimentoBetween(inizio, fine)).thenReturn(List.of(testMetadata));
        when(mockAnnotazioneRepo.findById(testId)).thenReturn(Optional.of(testAnnotazione));

        assertEquals(1, service.trovaPerPeriodo(inizio, fine).size());
    }

    // --- trovaPubbliche ---

    @Test
    void trovaPubbliche_ritornaLista() {
        testMetadata.setPubblica(true);
        when(mockMetadataRepo.findByPubblica(true)).thenReturn(List.of(testMetadata));
        when(mockAnnotazioneRepo.findById(testId)).thenReturn(Optional.of(testAnnotazione));

        assertEquals(1, service.trovaPubbliche().size());
    }

    // --- trovaPerStato ---

    @Test
    void trovaPerStato_ritornaLista() {
        when(mockMetadataRepo.findByStato(StatoAnnotazione.INSERITA)).thenReturn(List.of(testMetadata));
        when(mockAnnotazioneRepo.findById(testId)).thenReturn(Optional.of(testAnnotazione));

        assertEquals(1, service.trovaPerStato(StatoAnnotazione.INSERITA).size());
    }

    // --- cercaPerTesto ---

    @Test
    void cercaPerTesto_trovaInValore() {
        when(mockAnnotazioneRepo.findByValoreNotaContaining("test")).thenReturn(List.of(testAnnotazione));
        when(mockMetadataRepo.findByDescrizioneContaining("test")).thenReturn(Collections.emptyList());
        when(mockAnnotazioneRepo.findById(testId)).thenReturn(Optional.of(testAnnotazione));
        when(mockMetadataRepo.findById(testId)).thenReturn(Optional.of(testMetadata));

        List<AnnotazioneCompleta> result = service.cercaPerTesto("test");
        assertEquals(1, result.size());
    }

    @Test
    void cercaPerTesto_trovaInDescrizione() {
        when(mockAnnotazioneRepo.findByValoreNotaContaining("desc")).thenReturn(Collections.emptyList());
        when(mockMetadataRepo.findByDescrizioneContaining("desc")).thenReturn(List.of(testMetadata));
        when(mockAnnotazioneRepo.findById(testId)).thenReturn(Optional.of(testAnnotazione));
        when(mockMetadataRepo.findById(testId)).thenReturn(Optional.of(testMetadata));

        List<AnnotazioneCompleta> result = service.cercaPerTesto("desc");
        assertEquals(1, result.size());
    }

    // --- esisteAnnotazione ---

    @Test
    void esisteAnnotazione_entrambeEsistono_ritornaTruetrue() {
        when(mockAnnotazioneRepo.existsById(testId)).thenReturn(true);
        when(mockMetadataRepo.existsById(testId)).thenReturn(true);
        assertTrue(service.esisteAnnotazione(testId));
    }

    @Test
    void esisteAnnotazione_solounona_ritornaFalse() {
        when(mockAnnotazioneRepo.existsById(testId)).thenReturn(true);
        when(mockMetadataRepo.existsById(testId)).thenReturn(false);
        assertFalse(service.esisteAnnotazione(testId));
    }

    // --- contaAnnotazioni ---

    @Test
    void contaAnnotazioni_ritornaConteggio() {
        when(mockAnnotazioneRepo.count()).thenReturn(5L);
        assertEquals(5L, service.contaAnnotazioni());
    }

    // --- contaAnnotazioniPerUtente ---

    @Test
    void contaAnnotazioniPerUtente_ritornaConteggio() {
        when(mockMetadataRepo.countByUtenteCreazione("user1")).thenReturn(3L);
        assertEquals(3L, service.contaAnnotazioniPerUtente("user1"));
    }

    // --- eliminaAnnotazione ---

    @Test
    void eliminaAnnotazione_esiste_elimina() {
        when(mockAnnotazioneRepo.existsById(testId)).thenReturn(true);
        when(mockMetadataRepo.existsById(testId)).thenReturn(true);

        service.eliminaAnnotazione(testId);

        verify(mockAnnotazioneRepo).deleteById(testId);
        verify(mockMetadataRepo).deleteById(testId);
    }

    @Test
    void eliminaAnnotazione_nonEsiste_nonChiamaDelete() {
        when(mockAnnotazioneRepo.existsById(testId)).thenReturn(false);
        when(mockMetadataRepo.existsById(testId)).thenReturn(true);

        service.eliminaAnnotazione(testId);

        verify(mockAnnotazioneRepo, never()).deleteById(any());
    }

    // --- impostaVisibilitaPubblica ---

    @Test
    void impostaVisibilitaPubblica_metadataTrovata_aggiorna() {
        when(mockMetadataRepo.findById(testId)).thenReturn(Optional.of(testMetadata));
        when(mockMetadataRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.impostaVisibilitaPubblica(testId, true, "user1");

        verify(mockMetadataRepo).save(argThat(m -> m.getPubblica()));
    }

    @Test
    void impostaVisibilitaPubblica_metadataNonTrovata_lancia() {
        when(mockMetadataRepo.findById(testId)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class,
                () -> service.impostaVisibilitaPubblica(testId, true, "user1"));
    }

    // --- impostaCategoria ---

    @Test
    void impostaCategoria_metadataTrovata_aggiorna() {
        when(mockMetadataRepo.findById(testId)).thenReturn(Optional.of(testMetadata));
        when(mockMetadataRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.impostaCategoria(testId, "Lavoro", "user1");

        verify(mockMetadataRepo).save(argThat(m -> "Lavoro".equals(m.getCategoria())));
    }

    @Test
    void impostaCategoria_metadataNonTrovata_lancia() {
        when(mockMetadataRepo.findById(testId)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class,
                () -> service.impostaCategoria(testId, "X", "user1"));
    }

    // --- impostaTags ---

    @Test
    void impostaTags_metadataTrovata_aggiorna() {
        when(mockMetadataRepo.findById(testId)).thenReturn(Optional.of(testMetadata));
        when(mockMetadataRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.impostaTags(testId, "tag1,tag2", "user1");

        verify(mockMetadataRepo).save(argThat(m -> "tag1,tag2".equals(m.getTags())));
    }

    @Test
    void impostaTags_metadataNonTrovata_lancia() {
        when(mockMetadataRepo.findById(testId)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class,
                () -> service.impostaTags(testId, "tag", "user1"));
    }

    // --- impostaPriorita ---

    @Test
    void impostaPriorita_metadataTrovata_aggiorna() {
        when(mockMetadataRepo.findById(testId)).thenReturn(Optional.of(testMetadata));
        when(mockMetadataRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.impostaPriorita(testId, 3, "user1");

        verify(mockMetadataRepo).save(argThat(m -> Integer.valueOf(3).equals(m.getPriorita())));
    }

    @Test
    void impostaPriorita_metadataNonTrovata_lancia() {
        when(mockMetadataRepo.findById(testId)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class,
                () -> service.impostaPriorita(testId, 5, "user1"));
    }
}
