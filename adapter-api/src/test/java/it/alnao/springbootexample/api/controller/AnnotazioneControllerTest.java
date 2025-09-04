package it.alnao.springbootexample.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.alnao.springbootexample.api.dto.AggiornaAnnotazioneRequest;
import it.alnao.springbootexample.api.dto.AnnotazioneResponse;
import it.alnao.springbootexample.api.dto.CreaAnnotazioneRequest;
import it.alnao.springbootexample.port.domain.AnnotazioneCompleta;
import it.alnao.springbootexample.port.service.AnnotazioneService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AnnotazioneControllerTest {
    private MockMvc mockMvc;
    @Mock
    private AnnotazioneService annotazioneService;
    @InjectMocks
    private AnnotazioneController annotazioneController;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(annotazioneController).build();
    }

    @Test
    void creaAnnotazione_success() throws Exception {
        CreaAnnotazioneRequest request = new CreaAnnotazioneRequest();
        request.setValoreNota("test");
        request.setDescrizione("desc");
        request.setUtente("utente");
        AnnotazioneCompleta entity = new AnnotazioneCompleta();
        entity.setAnnotazione(new it.alnao.springbootexample.port.domain.Annotazione());
        entity.getAnnotazione().setId(UUID.randomUUID());
        when(annotazioneService.creaAnnotazione(any(), any(), any())).thenReturn(entity);
        when(annotazioneService.trovaPerID(any())).thenReturn(Optional.of(entity));
        mockMvc.perform(post("/api/annotazioni")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void ottieniTutteLeAnnotazioni_success() throws Exception {
        when(annotazioneService.trovaTutte()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/annotazioni"))
                .andExpect(status().isOk());
    }

    @Test
    void ottieniAnnotazionePerID_found() throws Exception {
        UUID id = UUID.randomUUID();
        AnnotazioneCompleta entity = new AnnotazioneCompleta();
        entity.setAnnotazione(new it.alnao.springbootexample.port.domain.Annotazione());
        entity.getAnnotazione().setId(id);
        when(annotazioneService.trovaPerID(eq(id))).thenReturn(Optional.of(entity));
        mockMvc.perform(get("/api/annotazioni/" + id))
                .andExpect(status().isOk());
    }

    @Test
    void ottieniAnnotazionePerID_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(annotazioneService.trovaPerID(eq(id))).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/annotazioni/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void aggiornaAnnotazione_success() throws Exception {
        UUID id = UUID.randomUUID();
        AggiornaAnnotazioneRequest request = new AggiornaAnnotazioneRequest();
        request.setId(id); // <--- AGGIUNTO per superare la validazione @NotNull
        request.setValoreNota("modificata");
        request.setDescrizione("desc");
        request.setUtente("utente");
        AnnotazioneCompleta entity = new AnnotazioneCompleta();
        entity.setAnnotazione(new it.alnao.springbootexample.port.domain.Annotazione());
        entity.getAnnotazione().setId(id);
        when(annotazioneService.aggiornaAnnotazione(eq(id), any(), any(), any())).thenReturn(entity);
        when(annotazioneService.trovaPerID(eq(id))).thenReturn(Optional.of(entity));
        mockMvc.perform(put("/api/annotazioni/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void eliminaAnnotazione_success() throws Exception {
        UUID id = UUID.randomUUID();
        when(annotazioneService.esisteAnnotazione(eq(id))).thenReturn(true);
        doNothing().when(annotazioneService).eliminaAnnotazione(eq(id));
        mockMvc.perform(delete("/api/annotazioni/" + id))
                .andExpect(status().isNoContent());
    }

    @Test
    void eliminaAnnotazione_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(annotazioneService.esisteAnnotazione(eq(id))).thenReturn(false);
        mockMvc.perform(delete("/api/annotazioni/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void cercaAnnotazioni_success() throws Exception {
        when(annotazioneService.cercaPerTesto(any())).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/annotazioni/cerca?testo=test"))
                .andExpect(status().isOk());
    }

    @Test
    void ottieniAnnotazioniPerUtente_success() throws Exception {
        when(annotazioneService.trovaPerUtente(any())).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/annotazioni/utente/utente1"))
                .andExpect(status().isOk());
    }

    @Test
    void ottieniAnnotazioniPerCategoria_success() throws Exception {
        when(annotazioneService.trovaPerCategoria(any())).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/annotazioni/categoria/cat1"))
                .andExpect(status().isOk());
    }

    @Test
    void ottieniAnnotazioniPubbliche_success() throws Exception {
        when(annotazioneService.trovaPubbliche()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/annotazioni/pubbliche"))
                .andExpect(status().isOk());
    }

    @Test
    void ottieniStatistiche_success() throws Exception {
        when(annotazioneService.contaAnnotazioni()).thenReturn(5L);
        mockMvc.perform(get("/api/annotazioni/statistiche"))
                .andExpect(status().isOk());
    }
}
