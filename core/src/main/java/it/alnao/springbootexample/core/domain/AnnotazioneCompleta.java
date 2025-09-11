package it.alnao.springbootexample.core.domain;

import java.util.UUID;

/**
 * DTO per l'annotazione completa (combina dati NoSQL e SQL)
 */
public class AnnotazioneCompleta {
    
    private Annotazione annotazione;
    private AnnotazioneMetadata metadata;
    
    public AnnotazioneCompleta() {}
    
    public AnnotazioneCompleta(Annotazione annotazione, AnnotazioneMetadata metadata) {
        this.annotazione = annotazione;
        this.metadata = metadata;
    }
    
    // Getters and Setters
    public Annotazione getAnnotazione() {
        return annotazione;
    }
    
    public void setAnnotazione(Annotazione annotazione) {
        this.annotazione = annotazione;
    }
    
    public AnnotazioneMetadata getMetadata() {
        return metadata;
    }
    
    public void setMetadata(AnnotazioneMetadata metadata) {
        this.metadata = metadata;
    }
    
    // Metodi di convenienza
    public UUID getId() {
        return annotazione != null ? annotazione.getId() : null;
    }
    
    public String getValoreNota() {
        return annotazione != null ? annotazione.getValoreNota() : null;
    }
    
    public String getDescrizione() {
        return metadata != null ? metadata.getDescrizione() : null;
    }
    
    public String getUtenteCreazione() {
        return metadata != null ? metadata.getUtenteCreazione() : null;
    }
    
    public String getVersioneNota() {
        return annotazione != null ? annotazione.getVersioneNota() : null;
    }
    
    @Override
    public String toString() {
        return "AnnotazioneCompleta{" +
                "annotazione=" + annotazione +
                ", metadata=" + metadata +
                '}';
    }
}
