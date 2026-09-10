package it.alnao.springbootexample.mongodb.service;

import it.alnao.springbootexample.core.domain.Annotazione;
import it.alnao.springbootexample.core.domain.AnnotazioneCompleta;
import it.alnao.springbootexample.core.domain.AnnotazioneMetadata;
import it.alnao.springbootexample.core.repository.AnnotazioneMetadataRepository;
import it.alnao.springbootexample.core.repository.AnnotazioneRepository;
import it.alnao.springbootexample.core.service.AnnotazioneLockService;
import it.alnao.springbootexample.mongodb.repository.AnnotazioneStoricoMongoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * L'aggiornamento non gestisce prenotazioni: la verifica che la modifica provenga
 * dal proprietario della prenotazione vive nel core ed è unica per tutti i profili.
 * Questi test fissano che l'adapter non acquisisca né rilasci lock per conto suo.
 */
class AnnotazioneServiceImplLockTest {

    @Mock AnnotazioneRepository annotazioneRepository;
    @Mock AnnotazioneMetadataRepository metadataRepository;
    @Mock AnnotazioneStoricoMongoRepository storicoMongoRepository;
    @InjectMocks AnnotazioneServiceImpl service;

    private UUID id;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        id = UUID.randomUUID();
    }

    private void esistente() {
        Annotazione ann = new Annotazione(id, "1.0", "vecchio testo");
        AnnotazioneMetadata meta = new AnnotazioneMetadata(id, "1.0", "mario", "descrizione");
        meta.setDataUltimaModifica(LocalDateTime.now().minusDays(1));
        lenient().when(annotazioneRepository.findById(id)).thenReturn(Optional.of(ann));
        lenient().when(metadataRepository.findById(id)).thenReturn(Optional.of(meta));
        lenient().when(annotazioneRepository.save(any(Annotazione.class))).thenAnswer(i -> i.getArgument(0));
        lenient().when(metadataRepository.save(any(AnnotazioneMetadata.class))).thenAnswer(i -> i.getArgument(0));
    }

    // ---- Nessuna dipendenza dal servizio di lock ----

    @Test
    void servizio_nonDipendeDalServizioDiLock() {
        for (Constructor<?> c : AnnotazioneServiceImpl.class.getConstructors()) {
            for (Class<?> parametro : c.getParameterTypes()) {
                assertNotEquals(AnnotazioneLockService.class, parametro,
                        "l'adapter non deve dipendere dal servizio di lock: la verifica vive nel core");
            }
        }
    }

    // ---- Validazione input ----

    @Test
    void aggiornaAnnotazione_conUtenteNullo_lanciaIllegalArgument() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.aggiornaAnnotazione(id, "nuovo", "descr", null));
        assertEquals("Utente non può essere null", e.getMessage());
        verifyNoInteractions(annotazioneRepository, metadataRepository, storicoMongoRepository);
    }

    @Test
    void aggiornaAnnotazione_suAnnotazioneInesistente_lanciaEccezione() {
        when(annotazioneRepository.findById(id)).thenReturn(Optional.empty());
        when(metadataRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class,
                () -> service.aggiornaAnnotazione(id, "nuovo", "descr", "mario"));
    }

    // ---- Aggiornamento ----

    @Test
    void aggiornaAnnotazione_applicaLaModificaEIncrementaLaVersione() {
        esistente();

        AnnotazioneCompleta risultato = service.aggiornaAnnotazione(id, "nuovo testo", "nuova descr", "mario");

        assertEquals("nuovo testo", risultato.getAnnotazione().getValoreNota());
        assertNotEquals("1.0", risultato.getAnnotazione().getVersioneNota(),
                "la versione della nota deve essere incrementata");
        assertEquals("mario", risultato.getMetadata().getUtenteUltimaModifica());
        verify(storicoMongoRepository).save(any());
    }

    @Test
    void aggiornaAnnotazione_daUtenteDiversoDalCreatore_vieneApplicato() {
        // La contesa fra utenti è decisa dal core: l'adapter non rifiuta nulla.
        esistente();

        AnnotazioneCompleta risultato = service.aggiornaAnnotazione(id, "testo di luigi", "descr", "luigi");

        assertEquals("testo di luigi", risultato.getAnnotazione().getValoreNota());
        assertEquals("luigi", risultato.getMetadata().getUtenteUltimaModifica());
    }
}
