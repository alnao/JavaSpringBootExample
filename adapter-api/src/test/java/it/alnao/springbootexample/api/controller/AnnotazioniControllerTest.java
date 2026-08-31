package it.alnao.springbootexample.api.controller;

import it.alnao.springbootexample.api.dto.AnnotazioneResponse;
import it.alnao.springbootexample.api.dto.CreaAnnotazioneRequest;
import it.alnao.springbootexample.core.domain.Annotazione;
import it.alnao.springbootexample.core.domain.AnnotazioneCompleta;
import it.alnao.springbootexample.core.domain.AnnotazioneMetadata;
import it.alnao.springbootexample.core.portService.AnnotazioniPortService;
import it.alnao.springbootexample.core.service.AnnotazioneLockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AnnotazioniControllerTest {

    @Mock AnnotazioniPortService portService;
    @Mock AnnotazioneLockService lockService;

    AnnotazioniController controller;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        controller = new AnnotazioniController(portService, lockService);
        ReflectionTestUtils.setField(controller, "lockNumeroSecondiDefault", 42);
    }

    @Test
    void creaAnnotazione_returnsCreated() {
        AnnotazioneCompleta ac = buildCompleta();
        when(portService.creaAnnotazione(any(), anyString())).thenReturn(ac);
        CreaAnnotazioneRequest req = new CreaAnnotazioneRequest();
        req.setValoreNota("nota");
        req.setUtente("user");
        req.setDescrizione("desc");

        ResponseEntity<AnnotazioneResponse> resp = controller.creaAnnotazione(req);

        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        assertNotNull(resp.getBody());
    }

    @Test
    void ottieniTutteLeAnnotazioni_returnsOk() {
        when(portService.trovaTutte()).thenReturn(List.of(buildCompleta()));
        ResponseEntity<List<AnnotazioneResponse>> resp = controller.ottieniTutteLeAnnotazioni();
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertFalse(resp.getBody().isEmpty());
    }

    @Test
    void ottieniAnnotazionePerID_whenFound_returnsOk() {
        UUID id = UUID.randomUUID();
        when(portService.trovaPerID(id)).thenReturn(Optional.of(buildCompleta()));
        ResponseEntity<AnnotazioneResponse> resp = controller.ottieniAnnotazionePerID(id);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

    @Test
    void ottieniAnnotazionePerID_whenNotFound_returnsNotFound() {
        UUID id = UUID.randomUUID();
        when(portService.trovaPerID(id)).thenReturn(Optional.empty());
        ResponseEntity<AnnotazioneResponse> resp = controller.ottieniAnnotazionePerID(id);
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
    }

    @Test
    void eliminaAnnotazione_returnsNoContent() {
        UUID id = UUID.randomUUID();
        doNothing().when(portService).eliminaAnnotazione(id);
        ResponseEntity<Void> resp = controller.eliminaAnnotazione(id);
        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
    }

    @Test
    void cercaAnnotazioni_returnsOk() {
        when(portService.cercaPerTesto("test")).thenReturn(List.of(buildCompleta()));
        ResponseEntity<List<AnnotazioneResponse>> resp = controller.cercaAnnotazioni("test");
        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

    @Test
    void ottieniAnnotazioniPerUtente_returnsOk() {
        when(portService.trovaPerUtente("user")).thenReturn(List.of(buildCompleta()));
        ResponseEntity<List<AnnotazioneResponse>> resp = controller.ottieniAnnotazioniPerUtente("user");
        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

    @Test
    void ottieniAnnotazioniPerCategoria_returnsOk() {
        when(portService.trovaPerCategoria("cat")).thenReturn(List.of(buildCompleta()));
        ResponseEntity<List<AnnotazioneResponse>> resp = controller.ottieniAnnotazioniPerCategoria("cat");
        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

    @Test
    void ottieniAnnotazioniPubbliche_returnsOk() {
        when(portService.trovaPubbliche()).thenReturn(List.of(buildCompleta()));
        ResponseEntity<List<AnnotazioneResponse>> resp = controller.ottieniAnnotazioniPubbliche();
        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

    @Test
    void ottieniTransizioniStato_returnsOk() {
        ResponseEntity<?> resp = controller.ottieniTransizioniStato();
        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

    private AnnotazioneCompleta buildCompleta() {
        UUID id = UUID.randomUUID();
        Annotazione ann = new Annotazione(id, "1.0", "nota");
        AnnotazioneMetadata meta = new AnnotazioneMetadata();
        meta.setId(id);
        meta.setVersioneNota("1.0");
        meta.setUtenteCreazione("user");
        meta.setDataInserimento(LocalDateTime.now());
        meta.setDataUltimaModifica(LocalDateTime.now());
        meta.setStato("INSERITA");
        return new AnnotazioneCompleta(ann, meta);
    }
}
