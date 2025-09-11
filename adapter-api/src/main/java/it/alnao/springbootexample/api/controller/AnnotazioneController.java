package it.alnao.springbootexample.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.alnao.springbootexample.api.dto.AggiornaAnnotazioneRequest;
import it.alnao.springbootexample.api.dto.AnnotazioneResponse;
import it.alnao.springbootexample.api.dto.CreaAnnotazioneRequest;
import it.alnao.springbootexample.api.mapper.AnnotazioneMapper;
import it.alnao.springbootexample.core.domain.AnnotazioneCompleta;
import it.alnao.springbootexample.core.portService.AnnotazioniPortService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Controller REST per la gestione delle annotazioni
 */
@RestController
@RequestMapping("/api/annotazioni")
@CrossOrigin(origins = "*")
@Tag(name = "Annotazioni", description = "API per la gestione delle annotazioni")
public class AnnotazioneController {
    
    private static final Logger logger = LoggerFactory.getLogger(AnnotazioneController.class);
    
    @Autowired
    private AnnotazioniPortService annotazioniPortService;
    
    @Operation(summary = "Crea una nuova annotazione")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Annotazione creata con successo"),
        @ApiResponse(responseCode = "400", description = "Dati di input non validi")
    })
    @PostMapping
    public ResponseEntity<AnnotazioneResponse> creaAnnotazione(
            @Valid @RequestBody CreaAnnotazioneRequest request) {
        logger.info("POST /api/annotazioni - Creazione annotazione per utente: {}, valore: {}", request.getUtente(), request.getValoreNota());
        
        AnnotazioneCompleta annotazioneCompleta = annotazioniPortService.creaAnnotazione(AnnotazioneMapper.fromCreateRequest(request),request.getUtente());

        logger.info("POST /api/annotazioni - Annotazione creata con successo, ID: {}", annotazioneCompleta.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AnnotazioneMapper.toResponse(annotazioneCompleta));
    }
    
    @Operation(summary = "Ottieni tutte le annotazioni")
    @GetMapping
    public ResponseEntity<List<AnnotazioneResponse>> ottieniTutteLeAnnotazioni() {
        logger.info("GET /api/annotazioni - Richiesta tutte le annotazioni");
        List<AnnotazioneCompleta> annotazioni = annotazioniPortService.trovaTutte();
        return ResponseEntity.ok(AnnotazioneMapper.toResponseList(annotazioni));
    }
    
    @Operation(summary = "Ottieni un'annotazione per ID")
    @GetMapping("/{id}")
    public ResponseEntity<AnnotazioneResponse> ottieniAnnotazionePerID(
            @Parameter(description = "ID dell'annotazione")
            @PathVariable UUID id) {
        
        logger.info("GET /api/annotazioni/{} - Richiesta annotazione per ID", id);
        return annotazioniPortService.trovaPerID(id)
                .map(annotazione -> ResponseEntity.ok(AnnotazioneMapper.toResponse(annotazione)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @Operation(summary = "Aggiorna un'annotazione esistente")
    @PutMapping("/{id}")
    public ResponseEntity<AnnotazioneResponse> aggiornaAnnotazione(
            @Parameter(description = "ID dell'annotazione")
            @PathVariable UUID id,
            @Valid @RequestBody AggiornaAnnotazioneRequest request) {
        logger.info("PUT /api/annotazioni/{} - Aggiornamento annotazione per utente: {}", id, request.getUtente());
        request.setId(id);
        AnnotazioneCompleta annotazioneAggiornata = annotazioniPortService.aggiornaAnnotazione(AnnotazioneMapper.fromUpdateRequest(request),request.getUtente());
        logger.info("PUT /api/annotazioni/{} - Annotazione aggiornata con successo", id);
        return ResponseEntity.ok(AnnotazioneMapper.toResponse(annotazioneAggiornata));
    }
    
    @Operation(summary = "Elimina un'annotazione")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminaAnnotazione(
            @Parameter(description = "ID dell'annotazione")
            @PathVariable UUID id) {
        logger.info("DELETE /api/annotazioni/{} - Eliminazione annotazione", id);
        annotazioniPortService.eliminaAnnotazione(id);
        logger.info("DELETE /api/annotazioni/{} - Annotazione eliminata con successo", id);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(summary = "Cerca annotazioni per testo")
    @GetMapping("/cerca")
    public ResponseEntity<List<AnnotazioneResponse>> cercaAnnotazioni(
            @Parameter(description = "Testo da cercare")
            @RequestParam String testo) {
        
        logger.info("GET /api/annotazioni/cerca?testo={} - Ricerca annotazioni per testo", testo);
        List<AnnotazioneCompleta> annotazioni = annotazioniPortService.cercaPerTesto(testo);
        return ResponseEntity.ok(AnnotazioneMapper.toResponseList(annotazioni));
    }
    
    @Operation(summary = "Ottieni annotazioni per utente")
    @GetMapping("/utente/{utente}")
    public ResponseEntity<List<AnnotazioneResponse>> ottieniAnnotazioniPerUtente(
            @Parameter(description = "Nome utente")
            @PathVariable String utente) {
        
        logger.info("GET /api/annotazioni/utente/{} - Richiesta annotazioni per utente", utente);
        List<AnnotazioneCompleta> annotazioni = annotazioniPortService.trovaPerUtente(utente);
        return ResponseEntity.ok(AnnotazioneMapper.toResponseList(annotazioni));
    }
    
    @Operation(summary = "Ottieni annotazioni per categoria")
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<AnnotazioneResponse>> ottieniAnnotazioniPerCategoria(
            @Parameter(description = "Nome categoria")
            @PathVariable String categoria) {
        
        logger.info("GET /api/annotazioni/categoria/{} - Richiesta annotazioni per categoria", categoria);
        List<AnnotazioneCompleta> annotazioni = annotazioniPortService.trovaPerCategoria(categoria);
        return ResponseEntity.ok(AnnotazioneMapper.toResponseList(annotazioni));
    }
    
    @Operation(summary = "Ottieni annotazioni pubbliche")
    @GetMapping("/pubbliche")
    public ResponseEntity<List<AnnotazioneResponse>> ottieniAnnotazioniPubbliche() {
        logger.info("GET /api/annotazioni/pubbliche - Richiesta annotazioni pubbliche");
        List<AnnotazioneCompleta> annotazioni = annotazioniPortService.trovaPubbliche();
        return ResponseEntity.ok(AnnotazioneMapper.toResponseList(annotazioni));
    }
    
    @Operation(summary = "Ottieni statistiche delle annotazioni")
    @GetMapping("/statistiche")
    public ResponseEntity<StatisticheResponse> ottieniStatistiche() {
        logger.info("GET /api/annotazioni/statistiche - Richiesta statistiche annotazioni");
        long totaleAnnotazioni = annotazioniPortService.contaAnnotazioni();
        
        StatisticheResponse statistiche = new StatisticheResponse();
        statistiche.setTotaleAnnotazioni(totaleAnnotazioni);
        statistiche.setDataGenerazione(LocalDateTime.now());
        
        return ResponseEntity.ok(statistiche);
    }
    
    // Classe interna per le statistiche
    public static class StatisticheResponse {
        private long totaleAnnotazioni;
        private LocalDateTime dataGenerazione;
        
        public long getTotaleAnnotazioni() {
            return totaleAnnotazioni;
        }
        
        public void setTotaleAnnotazioni(long totaleAnnotazioni) {
            this.totaleAnnotazioni = totaleAnnotazioni;
        }
        
        public LocalDateTime getDataGenerazione() {
            return dataGenerazione;
        }
        
        public void setDataGenerazione(LocalDateTime dataGenerazione) {
            this.dataGenerazione = dataGenerazione;
        }
    }
}
