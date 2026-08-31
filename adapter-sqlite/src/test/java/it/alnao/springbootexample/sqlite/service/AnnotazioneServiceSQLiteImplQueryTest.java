package it.alnao.springbootexample.sqlite.service;

import it.alnao.springbootexample.core.domain.AnnotazioneCompleta;
import it.alnao.springbootexample.core.domain.AnnotazioneMetadata;
import it.alnao.springbootexample.core.domain.StatoAnnotazione;
import it.alnao.springbootexample.sqlite.entity.AnnotazioneMetadataSQLiteEntity;
import it.alnao.springbootexample.sqlite.entity.AnnotazioneSQLiteEntity;
import it.alnao.springbootexample.sqlite.repository.AnnotazioneMetadataSQLiteRepository;
import it.alnao.springbootexample.sqlite.repository.AnnotazioneSQLiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Copre i metodi di ricerca e di aggiornamento puntuale del service SQLite,
 * complementari a quelli gia' verificati in {@link AnnotazioneServiceSQLiteImplTest}.
 */
class AnnotazioneServiceSQLiteImplQueryTest {

    @Mock AnnotazioneSQLiteRepository annotazioneRepository;
    @Mock AnnotazioneMetadataSQLiteRepository metadataRepository;
    @InjectMocks AnnotazioneServiceSQLiteImpl service;

    private UUID idMario;
    private UUID idLuigi;
    private AnnotazioneSQLiteEntity annMario;
    private AnnotazioneSQLiteEntity annLuigi;
    private AnnotazioneMetadataSQLiteEntity metaMario;
    private AnnotazioneMetadataSQLiteEntity metaLuigi;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        idMario = UUID.randomUUID();
        idLuigi = UUID.randomUUID();

        annMario = new AnnotazioneSQLiteEntity(idMario, "spesa al supermercato", "1");
        annLuigi = new AnnotazioneSQLiteEntity(idLuigi, "riunione di lavoro", "1");

        AnnotazioneMetadata mMario = new AnnotazioneMetadata(idMario, "1", "mario", "lista della spesa");
        mMario.setCategoria("casa");
        mMario.setPubblica(true);
        mMario.setStato(StatoAnnotazione.INSERITA.getValue());
        mMario.setDataInserimento(LocalDateTime.of(2026, 1, 10, 12, 0));
        metaMario = new AnnotazioneMetadataSQLiteEntity(mMario);

        AnnotazioneMetadata mLuigi = new AnnotazioneMetadata(idLuigi, "1", "luigi", "verbale riunione");
        mLuigi.setCategoria("lavoro");
        mLuigi.setPubblica(false);
        mLuigi.setStato(StatoAnnotazione.MODIFICATA.getValue());
        mLuigi.setDataInserimento(LocalDateTime.of(2026, 3, 20, 9, 0));
        metaLuigi = new AnnotazioneMetadataSQLiteEntity(mLuigi);
    }

    /** Prepara i due repository con entrambe le annotazioni. */
    private void withTwoAnnotazioni() {
        when(annotazioneRepository.findAll()).thenReturn(List.of(annMario, annLuigi));
        when(metadataRepository.findById(idMario.toString())).thenReturn(Optional.of(metaMario));
        when(metadataRepository.findById(idLuigi.toString())).thenReturn(Optional.of(metaLuigi));
    }

    @Test
    void trovaTutte_mapsAnnotazioniAndMetadata() {
        withTwoAnnotazioni();
        List<AnnotazioneCompleta> result = service.trovaTutte();
        assertEquals(2, result.size());
        assertEquals("spesa al supermercato", result.get(0).getAnnotazione().getValoreNota());
        assertEquals("mario", result.get(0).getMetadata().getUtenteCreazione());
    }

    @Test
    void trovaTutte_whenMetadataMissing_leavesMetadataNull() {
        when(annotazioneRepository.findAll()).thenReturn(List.of(annMario));
        when(metadataRepository.findById(idMario.toString())).thenReturn(Optional.empty());
        List<AnnotazioneCompleta> result = service.trovaTutte();
        assertEquals(1, result.size());
        assertNull(result.get(0).getMetadata());
    }

    @Test
    void trovaTutte_whenNoAnnotazioni_returnsEmptyList() {
        when(annotazioneRepository.findAll()).thenReturn(List.of());
        assertTrue(service.trovaTutte().isEmpty());
    }

    @Test
    void trovaPerUtente_filtersOnUtenteCreazione() {
        withTwoAnnotazioni();
        List<AnnotazioneCompleta> result = service.trovaPerUtente("mario");
        assertEquals(1, result.size());
        assertEquals(idMario, result.get(0).getAnnotazione().getId());
    }

    @Test
    void trovaPerUtente_withNullUtente_returnsEmptyList() {
        withTwoAnnotazioni();
        assertTrue(service.trovaPerUtente(null).isEmpty());
    }

    @Test
    void trovaPerCategoria_filtersOnCategoria() {
        withTwoAnnotazioni();
        List<AnnotazioneCompleta> result = service.trovaPerCategoria("lavoro");
        assertEquals(1, result.size());
        assertEquals(idLuigi, result.get(0).getAnnotazione().getId());
    }

    @Test
    void trovaPerCategoria_withNullCategoria_returnsEmptyList() {
        withTwoAnnotazioni();
        assertTrue(service.trovaPerCategoria(null).isEmpty());
    }

    @Test
    void trovaPerPeriodo_keepsOnlyDatesInsideTheRange() {
        withTwoAnnotazioni();
        List<AnnotazioneCompleta> result = service.trovaPerPeriodo(
                LocalDateTime.of(2026, 1, 1, 0, 0), LocalDateTime.of(2026, 2, 1, 0, 0));
        assertEquals(1, result.size());
        assertEquals(idMario, result.get(0).getAnnotazione().getId());
    }

    @Test
    void trovaPerPeriodo_boundsAreInclusive() {
        withTwoAnnotazioni();
        List<AnnotazioneCompleta> result = service.trovaPerPeriodo(
                LocalDateTime.of(2026, 1, 10, 12, 0), LocalDateTime.of(2026, 1, 10, 12, 0));
        assertEquals(1, result.size());
    }

    @Test
    void cercaPerTesto_matchesValoreNota() {
        withTwoAnnotazioni();
        List<AnnotazioneCompleta> result = service.cercaPerTesto("supermercato");
        assertEquals(1, result.size());
        assertEquals(idMario, result.get(0).getAnnotazione().getId());
    }

    @Test
    void cercaPerTesto_matchesDescrizioneToo() {
        withTwoAnnotazioni();
        List<AnnotazioneCompleta> result = service.cercaPerTesto("verbale");
        assertEquals(1, result.size());
        assertEquals(idLuigi, result.get(0).getAnnotazione().getId());
    }

    @Test
    void cercaPerTesto_whenNothingMatches_returnsEmptyList() {
        withTwoAnnotazioni();
        assertTrue(service.cercaPerTesto("parola inesistente").isEmpty());
    }

    @Test
    void trovaPubbliche_keepsOnlyPublicOnes() {
        withTwoAnnotazioni();
        List<AnnotazioneCompleta> result = service.trovaPubbliche();
        assertEquals(1, result.size());
        assertEquals(idMario, result.get(0).getAnnotazione().getId());
    }

    @Test
    void eliminaAnnotazione_deletesFromBothRepositories() {
        service.eliminaAnnotazione(idMario);
        verify(annotazioneRepository).deleteById(idMario.toString());
        verify(metadataRepository).deleteById(idMario.toString());
    }

    @Test
    void esisteAnnotazione_delegatesToRepository() {
        when(annotazioneRepository.existsById(idMario.toString())).thenReturn(true);
        assertTrue(service.esisteAnnotazione(idMario));
    }

    @Test
    void contaAnnotazioni_delegatesToRepository() {
        when(annotazioneRepository.count()).thenReturn(12L);
        assertEquals(12L, service.contaAnnotazioni());
    }

    @Test
    void contaAnnotazioniPerUtente_countsFilteredResults() {
        withTwoAnnotazioni();
        assertEquals(1L, service.contaAnnotazioniPerUtente("mario"));
        assertEquals(0L, service.contaAnnotazioniPerUtente("peach"));
    }

    @Test
    void impostaVisibilitaPubblica_savesUpdatedMetadata() {
        when(metadataRepository.findById(idLuigi.toString())).thenReturn(Optional.of(metaLuigi));
        service.impostaVisibilitaPubblica(idLuigi, true, "peach");

        ArgumentCaptor<AnnotazioneMetadataSQLiteEntity> captor =
                ArgumentCaptor.forClass(AnnotazioneMetadataSQLiteEntity.class);
        verify(metadataRepository).save(captor.capture());
        assertTrue(captor.getValue().getPubblica());
        assertEquals("peach", captor.getValue().getUtenteUltimaModifica());
    }

    @Test
    void impostaVisibilitaPubblica_whenMetadataMissing_savesNothing() {
        when(metadataRepository.findById(idLuigi.toString())).thenReturn(Optional.empty());
        service.impostaVisibilitaPubblica(idLuigi, true, "peach");
        verify(metadataRepository, never()).save(any());
    }

    @Test
    void impostaCategoria_savesUpdatedMetadata() {
        when(metadataRepository.findById(idMario.toString())).thenReturn(Optional.of(metaMario));
        service.impostaCategoria(idMario, "archivio", "peach");

        ArgumentCaptor<AnnotazioneMetadataSQLiteEntity> captor =
                ArgumentCaptor.forClass(AnnotazioneMetadataSQLiteEntity.class);
        verify(metadataRepository).save(captor.capture());
        assertEquals("archivio", captor.getValue().getCategoria());
    }

    @Test
    void impostaCategoria_whenMetadataMissing_savesNothing() {
        when(metadataRepository.findById(idMario.toString())).thenReturn(Optional.empty());
        service.impostaCategoria(idMario, "archivio", "peach");
        verify(metadataRepository, never()).save(any());
    }

    @Test
    void impostaTags_savesUpdatedMetadata() {
        when(metadataRepository.findById(idMario.toString())).thenReturn(Optional.of(metaMario));
        service.impostaTags(idMario, "urgente,casa", "peach");

        ArgumentCaptor<AnnotazioneMetadataSQLiteEntity> captor =
                ArgumentCaptor.forClass(AnnotazioneMetadataSQLiteEntity.class);
        verify(metadataRepository).save(captor.capture());
        assertEquals("urgente,casa", captor.getValue().getTags());
    }

    @Test
    void impostaTags_whenMetadataMissing_savesNothing() {
        when(metadataRepository.findById(idMario.toString())).thenReturn(Optional.empty());
        service.impostaTags(idMario, "urgente", "peach");
        verify(metadataRepository, never()).save(any());
    }

    @Test
    void impostaPriorita_savesUpdatedMetadata() {
        when(metadataRepository.findById(idMario.toString())).thenReturn(Optional.of(metaMario));
        service.impostaPriorita(idMario, 5, "peach");

        ArgumentCaptor<AnnotazioneMetadataSQLiteEntity> captor =
                ArgumentCaptor.forClass(AnnotazioneMetadataSQLiteEntity.class);
        verify(metadataRepository).save(captor.capture());
        assertEquals(5, captor.getValue().getPriorita());
    }

    @Test
    void impostaPriorita_whenMetadataMissing_savesNothing() {
        when(metadataRepository.findById(idMario.toString())).thenReturn(Optional.empty());
        service.impostaPriorita(idMario, 5, "peach");
        verify(metadataRepository, never()).save(any());
    }

    @Test
    void trovaPerStato_filtersOnTheStatoValue() {
        withTwoAnnotazioni();
        List<AnnotazioneCompleta> result = service.trovaPerStato(StatoAnnotazione.INSERITA);
        assertEquals(1, result.size());
        assertEquals(idMario, result.get(0).getAnnotazione().getId());
    }

    @Test
    void trovaPerStato_returnsTheOtherStatoToo() {
        withTwoAnnotazioni();
        List<AnnotazioneCompleta> result = service.trovaPerStato(StatoAnnotazione.MODIFICATA);
        assertEquals(1, result.size());
        assertEquals(idLuigi, result.get(0).getAnnotazione().getId());
    }

    @Test
    void trovaPerStato_whenNoAnnotazioneIsInThatStato_returnsEmptyList() {
        withTwoAnnotazioni();
        assertTrue(service.trovaPerStato(StatoAnnotazione.BANNATA).isEmpty());
    }
}
