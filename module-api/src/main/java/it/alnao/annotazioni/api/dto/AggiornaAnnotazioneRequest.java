package it.alnao.annotazioni.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * DTO per l'aggiornamento di un'annotazione esistente
 */
public class AggiornaAnnotazioneRequest {
    
    @NotNull(message = "L'ID non può essere null")
    private UUID id;
    
    @NotBlank(message = "Il valore della nota non può essere vuoto")
    @Size(min = 1, max = 10000, message = "Il valore della nota deve essere tra 1 e 10000 caratteri")
    private String valoreNota;
    
    @NotBlank(message = "La descrizione non può essere vuota")
    @Size(min = 1, max = 500, message = "La descrizione deve essere tra 1 e 500 caratteri")
    private String descrizione;
    
    @NotBlank(message = "L'utente non può essere vuoto")
    @Size(min = 1, max = 100, message = "L'utente deve essere tra 1 e 100 caratteri")
    private String utente;
    
    @Size(max = 100, message = "La categoria deve essere massimo 100 caratteri")
    private String categoria;
    
    @Size(max = 500, message = "I tag devono essere massimo 500 caratteri")
    private String tags;
    
    private Boolean pubblica;
    private Integer priorita;
    
    // Constructors
    public AggiornaAnnotazioneRequest() {}
    
    public AggiornaAnnotazioneRequest(UUID id, String valoreNota, String descrizione, String utente) {
        this.id = id;
        this.valoreNota = valoreNota;
        this.descrizione = descrizione;
        this.utente = utente;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getValoreNota() {
        return valoreNota;
    }
    
    public void setValoreNota(String valoreNota) {
        this.valoreNota = valoreNota;
    }
    
    public String getDescrizione() {
        return descrizione;
    }
    
    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }
    
    public String getUtente() {
        return utente;
    }
    
    public void setUtente(String utente) {
        this.utente = utente;
    }
    
    public String getCategoria() {
        return categoria;
    }
    
    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
    
    public String getTags() {
        return tags;
    }
    
    public void setTags(String tags) {
        this.tags = tags;
    }
    
    public Boolean getPubblica() {
        return pubblica;
    }
    
    public void setPubblica(Boolean pubblica) {
        this.pubblica = pubblica;
    }
    
    public Integer getPriorita() {
        return priorita;
    }
    
    public void setPriorita(Integer priorita) {
        this.priorita = priorita;
    }
}
