package it.alnao.springbootexample.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.alnao.springbootexample.api.dto.AggiornaAnnotazioneRequest;
import it.alnao.springbootexample.api.dto.AnnotazioneResponse;
import it.alnao.springbootexample.api.dto.CambiaStatoAnnotazioneRequest;
import it.alnao.springbootexample.api.dto.CreaAnnotazioneRequest;
import it.alnao.springbootexample.api.dto.TransizioneStatoResponse;
import it.alnao.springbootexample.api.mapper.AnnotazioneMapper;
import it.alnao.springbootexample.api.mapper.TransizioneStatoMapper;
import it.alnao.springbootexample.core.domain.AnnotazioneCompleta;
import it.alnao.springbootexample.core.domain.StatoAnnotazione;
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
    
    @Operation(summary = "Ottieni annotazioni per stato")
    @GetMapping("/stato/{stato}")
    public ResponseEntity<List<AnnotazioneResponse>> ottieniAnnotazioniPerStato(
            @Parameter(description = "Stato delle annotazioni")
            @PathVariable String stato) {
        
        logger.info("GET /api/annotazioni/stato/{} - Richiesta annotazioni per stato", stato);
        try {
            StatoAnnotazione statoEnum = StatoAnnotazione.valueOf(stato.toUpperCase());
            List<AnnotazioneCompleta> annotazioni = annotazioniPortService.trovaPerStato(statoEnum);
            return ResponseEntity.ok(AnnotazioneMapper.toResponseList(annotazioni));
        } catch (IllegalArgumentException e) {
            logger.error("GET /api/annotazioni/stato/{} - Stato non valido: {}", stato, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
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

    @Operation(summary = "Ottieni tutte le transizioni di stato configurate")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista delle transizioni ottenuta con successo"),
        @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/transizioni-stato")
    public ResponseEntity<List<TransizioneStatoResponse>> ottieniTransizioniStato() {
        logger.info("GET /api/annotazioni/transizioni-stato - Richiesta lista transizioni di stato");
        
        try {
            List<TransizioneStatoResponse> transizioni = TransizioneStatoMapper.toResponseList(
                annotazioniPortService.listaCambiamentiStati()
            );
            
            logger.info("GET /api/annotazioni/transizioni-stato - Restituite {} transizioni", transizioni.size());
            return ResponseEntity.ok(transizioni);
            
        } catch (Exception e) {
            logger.error("GET /api/annotazioni/transizioni-stato - Errore: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Cambia lo stato di un'annotazione")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Stato cambiato con successo"),
        @ApiResponse(responseCode = "400", description = "Dati di input non validi o transizione non permessa"),
        @ApiResponse(responseCode = "404", description = "Annotazione o utente non trovato"),
        @ApiResponse(responseCode = "403", description = "Utente non autorizzato per questa transizione")
    })
    @PatchMapping("/{id}/stato")
    public ResponseEntity<AnnotazioneResponse> cambiaStato(
            @Parameter(description = "ID dell'annotazione")
            @PathVariable UUID id,
            @Valid @RequestBody CambiaStatoAnnotazioneRequest request) {
        
        logger.info("PATCH /api/annotazioni/{}/stato - Cambio stato da {} a {} per utente: {}", 
                   id, request.getVecchioStato(), request.getNuovoStato(), request.getUtente());
        
        try {
            StatoAnnotazione vecchioStato = StatoAnnotazione.valueOf(request.getVecchioStato());
            StatoAnnotazione nuovoStato = StatoAnnotazione.valueOf(request.getNuovoStato());
            
            AnnotazioneCompleta annotazioneAggiornata = annotazioniPortService.cambiaStato(
                id, vecchioStato, nuovoStato, request.getUtente()
            );
            
            logger.info("PATCH /api/annotazioni/{}/stato - Stato cambiato con successo da {} a {}", 
                       id, request.getVecchioStato(), request.getNuovoStato());
            
            return ResponseEntity.ok(AnnotazioneMapper.toResponse(annotazioneAggiornata));
            
        } catch (IllegalArgumentException e) {
            logger.error("PATCH /api/annotazioni/{}/stato - Errore validazione: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            logger.error("PATCH /api/annotazioni/{}/stato - Transizione non permessa: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            logger.error("PATCH /api/annotazioni/{}/stato - Errore interno: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
