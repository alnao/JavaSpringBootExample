package it.alnao.springbootexample.javafx.service;

import it.alnao.springbootexample.core.domain.Annotazione;
import it.alnao.springbootexample.core.domain.AnnotazioneCompleta;
import it.alnao.springbootexample.core.domain.AnnotazioneMetadata;
import it.alnao.springbootexample.core.domain.StatoAnnotazione;
import it.alnao.springbootexample.javafx.model.AnnotazioneViewModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ViewModelConverterServiceTest {

    private ViewModelConverterService converter;

    @BeforeEach
    void setup() {
        converter = new ViewModelConverterService();
    }

    @Test
    void toViewModel_copiesAnnotazioneAndMetadata() {
        UUID id = UUID.randomUUID();
        LocalDateTime data = LocalDateTime.of(2026, 3, 15, 9, 30);

        Annotazione ann = new Annotazione(id, "1.0", "il testo");
        AnnotazioneMetadata meta = new AnnotazioneMetadata(id, "1.0", "mario", "la descrizione");
        meta.setDataInserimento(data);
        meta.setStato(StatoAnnotazione.INSERITA.getValue());
        meta.setCategoria("lavoro");
        meta.setPriorita(3);
        meta.setPubblica(true);
        meta.setTags("urgente,nota");

        AnnotazioneViewModel vm = converter.toViewModel(new AnnotazioneCompleta(ann, meta));

        assertEquals(id, vm.getId());
        assertEquals("il testo", vm.getValoreNota());
        assertEquals("la descrizione", vm.getDescrizione());
        assertEquals("mario", vm.getUtenteCreazione());
        assertEquals(data, vm.getDataCreazione());
        assertEquals(StatoAnnotazione.INSERITA.getValue(), vm.getStato());
        assertEquals("lavoro", vm.getCategoria());
        assertEquals(3, vm.getPriorita());
        assertTrue(vm.getPubblica());
        assertEquals("urgente,nota", vm.getTags());
    }

    @Test
    void toViewModel_withNull_returnsNull() {
        assertNull(converter.toViewModel(null));
    }

    @Test
    void toViewModel_keepsTheDefaultsOfAnEmptyMetadata() {
        UUID id = UUID.randomUUID();
        Annotazione ann = new Annotazione(id, "1.0", "testo");
        AnnotazioneMetadata meta = new AnnotazioneMetadata();
        meta.setId(id);

        AnnotazioneViewModel vm = converter.toViewModel(new AnnotazioneCompleta(ann, meta));

        assertEquals(id, vm.getId());
        assertEquals("testo", vm.getValoreNota());
        assertNull(vm.getDescrizione());
        assertNull(vm.getCategoria());
    }
}
