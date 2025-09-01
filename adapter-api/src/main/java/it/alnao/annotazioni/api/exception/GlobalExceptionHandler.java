package it.alnao.annotazioni.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestore globale delle eccezioni per l'API REST
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Gestisce errori di conversione di parametri (es. UUID invalidi)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Parametro '%s' non valido: '%s'. Deve essere nel formato UUID corretto (es. 123e4567-e89b-12d3-a456-426614174000)", 
                ex.getName(), ex.getValue());
        
        ErrorResponse error = new ErrorResponse(
                "INVALID_PARAMETER_FORMAT",
                message,
                LocalDateTime.now()
        );
        
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Gestisce errori di validazione dei dati di input
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        ErrorResponse error = new ErrorResponse(
                "VALIDATION_ERROR",
                "Errori di validazione nei dati di input",
                LocalDateTime.now(),
                errors
        );

        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Gestisce IllegalArgumentException generiche
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorResponse error = new ErrorResponse(
                "INVALID_ARGUMENT",
                ex.getMessage(),
                LocalDateTime.now()
        );
        
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Gestisce RuntimeException generiche
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        ErrorResponse error = new ErrorResponse(
                "INTERNAL_ERROR",
                "Si è verificato un errore interno: " + ex.getMessage(),
                LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Gestisce eccezioni generiche non catturate
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
                "UNKNOWN_ERROR",
                "Si è verificato un errore imprevisto",
                LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Classe per la risposta di errore
     */
    public static class ErrorResponse {
        private String codice;
        private String messaggio;
        private LocalDateTime timestamp;
        private Map<String, String> dettagli;

        public ErrorResponse(String codice, String messaggio, LocalDateTime timestamp) {
            this.codice = codice;
            this.messaggio = messaggio;
            this.timestamp = timestamp;
        }

        public ErrorResponse(String codice, String messaggio, LocalDateTime timestamp, Map<String, String> dettagli) {
            this.codice = codice;
            this.messaggio = messaggio;
            this.timestamp = timestamp;
            this.dettagli = dettagli;
        }

        // Getters e Setters
        public String getCodice() {
            return codice;
        }

        public void setCodice(String codice) {
            this.codice = codice;
        }

        public String getMessaggio() {
            return messaggio;
        }

        public void setMessaggio(String messaggio) {
            this.messaggio = messaggio;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public Map<String, String> getDettagli() {
            return dettagli;
        }

        public void setDettagli(Map<String, String> dettagli) {
            this.dettagli = dettagli;
        }
    }
}
