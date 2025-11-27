package it.alnao.springbootexample.javafx.model;

import javafx.beans.property.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Modello view per JavaFX con proprietà observable
 */
@Data
public class AnnotazioneViewModel {
    
    private final ObjectProperty<UUID> id = new SimpleObjectProperty<>();
    private final StringProperty valoreNota = new SimpleStringProperty();
    private final StringProperty descrizione = new SimpleStringProperty();
    private final StringProperty utenteCreazione = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> dataCreazione = new SimpleObjectProperty<>();
    private final StringProperty stato = new SimpleStringProperty();
    private final StringProperty categoria = new SimpleStringProperty();
    private final IntegerProperty priorita = new SimpleIntegerProperty();
    private final BooleanProperty pubblica = new SimpleBooleanProperty();
    private final StringProperty tags = new SimpleStringProperty();

    public AnnotazioneViewModel() {
    }

    public AnnotazioneViewModel(UUID id, String valoreNota, String descrizione, 
                                String utenteCreazione, LocalDateTime dataCreazione,
                                String stato, String categoria, Integer priorita,
                                Boolean pubblica, String tags) {
        setId(id);
        setValoreNota(valoreNota);
        setDescrizione(descrizione);
        setUtenteCreazione(utenteCreazione);
        setDataCreazione(dataCreazione);
        setStato(stato);
        setCategoria(categoria);
        setPriorita(priorita);
        setPubblica(pubblica);
        setTags(tags);
    }

    // Property getters
    public ObjectProperty<UUID> idProperty() { return id; }
    public StringProperty valoreNotaProperty() { return valoreNota; }
    public StringProperty descrizioneProperty() { return descrizione; }
    public StringProperty utenteCreazioneProperty() { return utenteCreazione; }
    public ObjectProperty<LocalDateTime> dataCreazioneProperty() { return dataCreazione; }
    public StringProperty statoProperty() { return stato; }
    public StringProperty categoriaProperty() { return categoria; }
    public IntegerProperty prioritaProperty() { return priorita; }
    public BooleanProperty pubblicaProperty() { return pubblica; }
    public StringProperty tagsProperty() { return tags; }

    // Value getters
    public UUID getId() { return id.get(); }
    public String getValoreNota() { return valoreNota.get(); }
    public String getDescrizione() { return descrizione.get(); }
    public String getUtenteCreazione() { return utenteCreazione.get(); }
    public LocalDateTime getDataCreazione() { return dataCreazione.get(); }
    public String getStato() { return stato.get(); }
    public String getCategoria() { return categoria.get(); }
    public Integer getPriorita() { return priorita.get(); }
    public Boolean getPubblica() { return pubblica.get(); }
    public String getTags() { return tags.get(); }

    // Value setters
    public void setId(UUID value) { id.set(value); }
    public void setValoreNota(String value) { valoreNota.set(value); }
    public void setDescrizione(String value) { descrizione.set(value); }
    public void setUtenteCreazione(String value) { utenteCreazione.set(value); }
    public void setDataCreazione(LocalDateTime value) { dataCreazione.set(value); }
    public void setStato(String value) { stato.set(value); }
    public void setCategoria(String value) { categoria.set(value); }
    public void setPriorita(Integer value) { priorita.set(value); }
    public void setPubblica(Boolean value) { pubblica.set(value); }
    public void setTags(String value) { tags.set(value); }
}
