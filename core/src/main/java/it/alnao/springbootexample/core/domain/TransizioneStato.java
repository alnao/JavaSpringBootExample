package it.alnao.springbootexample.core.domain;

import it.alnao.springbootexample.core.domain.auth.UserRole;

/**
 * Rappresenta una transizione di stato con le regole di autorizzazione
 */
public class TransizioneStato {
    private final StatoAnnotazione statoPartenza;
    private final StatoAnnotazione statoArrivo;
    private final UserRole ruoloRichiesto;
    private final String descrizione;

    public TransizioneStato(StatoAnnotazione statoPartenza, StatoAnnotazione statoArrivo, 
                           UserRole ruoloRichiesto, String descrizione) {
        this.statoPartenza = statoPartenza;
        this.statoArrivo = statoArrivo;
        this.ruoloRichiesto = ruoloRichiesto;
        this.descrizione = descrizione;
    }

    public StatoAnnotazione getStatoPartenza() {
        return statoPartenza;
    }

    public StatoAnnotazione getStatoArrivo() {
        return statoArrivo;
    }

    public UserRole getRuoloRichiesto() {
        return ruoloRichiesto;
    }

    public String getDescrizione() {
        return descrizione;
    }

    @Override
    public String toString() {
        return "TransizioneStato{" +
                "da=" + statoPartenza +
                ", a=" + statoArrivo +
                ", ruolo=" + ruoloRichiesto +
                ", descrizione='" + descrizione + '\'' +
                '}';
    }
}
