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
import it.alnao.springbootexample.api.dto.ErrorResponse;
import it.alnao.springbootexample.api.dto.PrenotaAnnotazioneRequest;
import it.alnao.springbootexample.api.dto.PrenotaAnnotazioneResponse;
import it.alnao.springbootexample.api.dto.TransizioneStatoResponse;
import it.alnao.springbootexample.api.mapper.AnnotazioneMapper;
import it.alnao.springbootexample.api.mapper.TransizioneStatoMapper;
import it.alnao.springbootexample.core.domain.AnnotazioneCompleta;
import it.alnao.springbootexample.core.domain.StatoAnnotazione;
import it.alnao.springbootexample.core.exception.AnnotationLockedException;
import it.alnao.springbootexample.core.portService.AnnotazioniPortService;
import it.alnao.springbootexample.core.service.LockService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    //predo da una properties o config
    @Value("${gestione-annotazioni.prenotazione-lock.lock-expiration-seconds:42}")
    Integer lockNumeroSecondiDefault;
    
    private static final Logger logger = LoggerFactory.getLogger(AnnotazioneController.class);
    
    @Autowired
    private AnnotazioniPortService annotazioniPortService;
    
    @Autowired
    private LockService lockService;
    
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
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Annotazione aggiornata con successo"),
        @ApiResponse(responseCode = "409", description = "Annotazione bloccata da altro utente"),
        @ApiResponse(responseCode = "404", description = "Annotazione non trovata")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> aggiornaAnnotazione(
            @Parameter(description = "ID dell'annotazione")
            @PathVariable UUID id,
            @Valid @RequestBody AggiornaAnnotazioneRequest request) {
        logger.info("PUT /api/annotazioni/{} - Aggiornamento annotazione per utente: {}", id, request.getUtente());
        
        try {
            request.setId(id);
            AnnotazioneCompleta annotazioneAggiornata = annotazioniPortService.aggiornaAnnotazione(
                AnnotazioneMapper.fromUpdateRequest(request), request.getUtente());
            logger.info("PUT /api/annotazioni/{} - Annotazione aggiornata con successo", id);
            return ResponseEntity.ok(AnnotazioneMapper.toResponse(annotazioneAggiornata));
            
        } catch (AnnotationLockedException e) {
            logger.warn("PUT /api/annotazioni/{} - Lock non acquisito: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(e.getMessage(), "ANNOTATION_LOCKED"));
        }
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
        //logger.info("GET /api/annotazioni/transizioni-stato - Richiesta lista transizioni di stato");
        
        try {
            List<TransizioneStatoResponse> transizioni = TransizioneStatoMapper.toResponseList(
                annotazioniPortService.listaCambiamentiStati()
            );
            
            //logger.info("GET /api/annotazioni/transizioni-stato - Restituiti {} possibili cambi di stato", transizioni.size());
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
    
    @Operation(summary = "Prenota un'annotazione per modifica", 
               description = "Acquisisce un lock di XX secondi sull'annotazione per permettere la modifica esclusiva")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Annotazione prenotata con successo"),
        @ApiResponse(responseCode = "409", description = "Annotazione già bloccata da altro utente"),
        @ApiResponse(responseCode = "404", description = "Annotazione non trovata")
    })
    @PostMapping("/{id}/prenota")
    public ResponseEntity<?> prenotaAnnotazione(
            @Parameter(description = "ID dell'annotazione da prenotare")
            @PathVariable UUID id,
            @Valid @RequestBody PrenotaAnnotazioneRequest request) {
        
        logger.info("POST /api/annotazioni/{}/prenota - Prenotazione annotazione per utente: {}", id, request.getUtente());
        
        // Verifica che l'annotazione esista
        if (annotazioniPortService.trovaPerID(id).isEmpty()) {
            logger.warn("POST /api/annotazioni/{}/prenota - Annotazione non trovata", id);
            return ResponseEntity.notFound().build();
        }
        
        Integer numeroSecondi = request.getSecondi() != null ? request.getSecondi() : lockNumeroSecondiDefault;
        
        // Verifica se già bloccata
        if (lockService.isLocked(id)) {
            String owner = lockService.getOwner(id).orElse("unknown");
            
            // Se è bloccata dallo stesso utente, considera la prenotazione già attiva
            if (owner.equals(request.getUtente())) {
                logger.info("POST /api/annotazioni/{}/prenota - Annotazione già prenotata dallo stesso utente", id);
                PrenotaAnnotazioneResponse response = new PrenotaAnnotazioneResponse(
                    id, 
                    request.getUtente(), 
                    LocalDateTime.now(),
                    LocalDateTime.now().plusSeconds(numeroSecondi),
                    true,
                    "Annotazione già prenotata da te"
                );
                return ResponseEntity.ok(response);
            }
            
            // Bloccata da altro utente
            logger.warn("POST /api/annotazioni/{}/prenota - Annotazione già bloccata da: {}", id, owner);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                    "Annotazione già in modifica da: " + owner, 
                    "ANNOTATION_ALREADY_LOCKED"
                ));
        }
        
        // Tenta di acquisire il lock per XX secondi
        boolean lockAcquired = lockService.acquireLock(id, request.getUtente(), numeroSecondi);
        
        if (lockAcquired) {
            logger.info("POST /api/annotazioni/{}/prenota - Lock acquisito per {} secondi da utente: {}", 
                id, numeroSecondi, request.getUtente());
            
            PrenotaAnnotazioneResponse response = new PrenotaAnnotazioneResponse(
                id, 
                request.getUtente(), 
                LocalDateTime.now(),
                LocalDateTime.now().plusSeconds(numeroSecondi),
                true,
                "Annotazione prenotata con successo per " + numeroSecondi + " secondi"
            );
            
            return ResponseEntity.ok(response);
        } else {
            logger.warn("POST /api/annotazioni/{}/prenota - Impossibile acquisire lock per utente: {}", 
                id, request.getUtente());
            
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                    "Impossibile prenotare l'annotazione in questo momento", 
                    "LOCK_ACQUISITION_FAILED"
                ));
        }
    }
    
    @Operation(summary = "Rilascia la prenotazione di un'annotazione", 
               description = "Rilascia il lock su un'annotazione prenotata")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Lock rilasciato con successo"),
        @ApiResponse(responseCode = "404", description = "Annotazione non trovata"),
        @ApiResponse(responseCode = "409", description = "Lock non posseduto dall'utente")
    })
    @DeleteMapping("/{id}/prenota")
    public ResponseEntity<?> rilasciaPrenotazione(
            @Parameter(description = "ID dell'annotazione")
            @PathVariable UUID id,
            @Valid @RequestBody PrenotaAnnotazioneRequest request) {
        
        logger.info("DELETE /api/annotazioni/{}/prenota - Rilascio prenotazione per utente: {}", id, request.getUtente());
        
        // Verifica che l'annotazione esista
        if (annotazioniPortService.trovaPerID(id).isEmpty()) {
            logger.warn("DELETE /api/annotazioni/{}/prenota - Annotazione non trovata", id);
            return ResponseEntity.notFound().build();
        }
        
        // Verifica il proprietario del lock
        if (lockService.isLocked(id)) {
            String owner = lockService.getOwner(id).orElse("unknown");
            
            if (!owner.equals(request.getUtente())) {
                logger.warn("DELETE /api/annotazioni/{}/prenota - Tentativo di rilascio lock da utente non proprietario. Owner: {}, Richiedente: {}", 
                    id, owner, request.getUtente());
                
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(
                        "Non puoi rilasciare una prenotazione che non ti appartiene", 
                        "NOT_LOCK_OWNER"
                    ));
            }
        }
        
        // Rilascia il lock
        lockService.releaseLock(id, request.getUtente());
        logger.info("DELETE /api/annotazioni/{}/prenota - Lock rilasciato con successo da utente: {}", 
            id, request.getUtente());
        
        return ResponseEntity.noContent().build();
    }
    
    @Operation(summary = "Verifica lo stato di prenotazione di un'annotazione")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Stato prenotazione ottenuto con successo"),
        @ApiResponse(responseCode = "404", description = "Annotazione non trovata")
    })
    @GetMapping("/{id}/prenota/stato")
    public ResponseEntity<?> verificaStatoPrenotazione(
            @Parameter(description = "ID dell'annotazione")
            @PathVariable UUID id) {
        
        logger.info("GET /api/annotazioni/{}/prenota/stato - Verifica stato prenotazione", id);
        // Verifica che l'annotazione esista
        if (annotazioniPortService.trovaPerID(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        boolean isLocked = lockService.isLocked(id);
        String owner = lockService.getOwner(id).orElse(null);
        
        StatoPrenotazioneResponse response = new StatoPrenotazioneResponse(id, isLocked, owner);
        return ResponseEntity.ok(response);
    }
    
    // Classe interna per lo stato di prenotazione
    public static class StatoPrenotazioneResponse {
        private UUID annotazioneId;
        private boolean prenotata;
        private String utenteProprietario;
        
        public StatoPrenotazioneResponse(UUID annotazioneId, boolean prenotata, String utenteProprietario) {
            this.annotazioneId = annotazioneId;
            this.prenotata = prenotata;
            this.utenteProprietario = utenteProprietario;
        }
        
        public UUID getAnnotazioneId() {
            return annotazioneId;
        }
        
        public void setAnnotazioneId(UUID annotazioneId) {
            this.annotazioneId = annotazioneId;
        }
        
        public boolean isPrenotata() {
            return prenotata;
        }
        
        public void setPrenotata(boolean prenotata) {
            this.prenotata = prenotata;
        }
        
        public String getUtenteProprietario() {
            return utenteProprietario;
        }
        
        public void setUtenteProprietario(String utenteProprietario) {
            this.utenteProprietario = utenteProprietario;
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
