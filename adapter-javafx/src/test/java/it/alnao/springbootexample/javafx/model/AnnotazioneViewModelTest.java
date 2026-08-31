package it.alnao.springbootexample.javafx.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le property JavaFX usate qui (SimpleStringProperty e simili) non richiedono
 * l'inizializzazione del toolkit, quindi il view model e' verificabile headless.
 */
class AnnotazioneViewModelTest {

    private static final LocalDateTime DATA = LocalDateTime.of(2026, 3, 15, 9, 30);

    private AnnotazioneViewModel full(UUID id) {
        return new AnnotazioneViewModel(id, "il testo", "la descrizione", "mario",
                DATA, "INSERITA", "lavoro", 3, true, "urgente,nota");
    }

    @Test
    void allArgsConstructor_copiesEveryField() {
        UUID id = UUID.randomUUID();
        AnnotazioneViewModel vm = full(id);
        assertEquals(id, vm.getId());
        assertEquals("il testo", vm.getValoreNota());
        assertEquals("la descrizione", vm.getDescrizione());
        assertEquals("mario", vm.getUtenteCreazione());
        assertEquals(DATA, vm.getDataCreazione());
        assertEquals("INSERITA", vm.getStato());
        assertEquals("lavoro", vm.getCategoria());
        assertEquals(3, vm.getPriorita());
        assertTrue(vm.getPubblica());
        assertEquals("urgente,nota", vm.getTags());
    }

    @Test
    void defaultConstructor_leavesTheObjectPropertiesEmpty() {
        AnnotazioneViewModel vm = new AnnotazioneViewModel();
        assertNull(vm.getId());
        assertNull(vm.getValoreNota());
        assertNull(vm.getDescrizione());
        assertNull(vm.getDataCreazione());
        assertEquals(0, vm.getPriorita());
        assertFalse(vm.getPubblica());
    }

    @Test
    void settersAndGettersWork() {
        UUID id = UUID.randomUUID();
        AnnotazioneViewModel vm = new AnnotazioneViewModel();
        vm.setId(id);
        vm.setValoreNota("nuovo testo");
        vm.setDescrizione("nuova descrizione");
        vm.setUtenteCreazione("luigi");
        vm.setDataCreazione(DATA);
        vm.setStato("MODIFICATA");
        vm.setCategoria("casa");
        vm.setPriorita(5);
        vm.setPubblica(false);
        vm.setTags("a,b");

        assertEquals(id, vm.getId());
        assertEquals("nuovo testo", vm.getValoreNota());
        assertEquals("nuova descrizione", vm.getDescrizione());
        assertEquals("luigi", vm.getUtenteCreazione());
        assertEquals(DATA, vm.getDataCreazione());
        assertEquals("MODIFICATA", vm.getStato());
        assertEquals("casa", vm.getCategoria());
        assertEquals(5, vm.getPriorita());
        assertFalse(vm.getPubblica());
        assertEquals("a,b", vm.getTags());
    }

    @Test
    void propertyAccessors_exposeTheUnderlyingObservables() {
        UUID id = UUID.randomUUID();
        AnnotazioneViewModel vm = full(id);
        assertEquals(id, vm.idProperty().get());
        assertEquals("il testo", vm.valoreNotaProperty().get());
        assertEquals("la descrizione", vm.descrizioneProperty().get());
        assertEquals("mario", vm.utenteCreazioneProperty().get());
        assertEquals(DATA, vm.dataCreazioneProperty().get());
        assertEquals("INSERITA", vm.statoProperty().get());
        assertEquals("lavoro", vm.categoriaProperty().get());
        assertEquals(3, vm.prioritaProperty().get());
        assertTrue(vm.pubblicaProperty().get());
        assertEquals("urgente,nota", vm.tagsProperty().get());
    }

    @Test
    void propertiesAreObservable_settingAValueNotifiesTheListener() {
        AnnotazioneViewModel vm = new AnnotazioneViewModel();
        StringBuilder osservato = new StringBuilder();
        vm.valoreNotaProperty().addListener((obs, vecchio, nuovo) -> osservato.append(nuovo));

        vm.setValoreNota("cambiato");

        assertEquals("cambiato", osservato.toString());
    }

    @Test
    void propertyInstancesAreStableAcrossCalls() {
        AnnotazioneViewModel vm = new AnnotazioneViewModel();
        assertSame(vm.valoreNotaProperty(), vm.valoreNotaProperty());
        assertSame(vm.prioritaProperty(), vm.prioritaProperty());
    }
}
