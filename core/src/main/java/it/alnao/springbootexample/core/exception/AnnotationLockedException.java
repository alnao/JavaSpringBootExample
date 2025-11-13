package it.alnao.springbootexample.core.exception;

import java.util.UUID;

/**
 * Eccezione lanciata quando si tenta di modificare un'annotazione
 * già bloccata da un altro utente.
 */
public class AnnotationLockedException extends RuntimeException {
    
    private final UUID annotazioneId;
    private final String currentOwner;
    
    public AnnotationLockedException(UUID annotazioneId, String currentOwner) {
        super(String.format("Annotazione %s è in modifica da: %s", annotazioneId, currentOwner));
        this.annotazioneId = annotazioneId;
        this.currentOwner = currentOwner;
    }
    
    public AnnotationLockedException(String message) {
        super(message);
        this.annotazioneId = null;
        this.currentOwner = null;
    }
    
    public UUID getAnnotazioneId() {
        return annotazioneId;
    }
    
    public String getCurrentOwner() {
        return currentOwner;
    }
}
