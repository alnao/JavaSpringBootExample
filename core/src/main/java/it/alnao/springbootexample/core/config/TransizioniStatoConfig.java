package it.alnao.springbootexample.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configurazione per il caricamento delle transizioni di stato da file YAML
 */
@Configuration
@ConfigurationProperties(prefix = "")
public class TransizioniStatoConfig {

    private List<TransizioneYaml> transizioni;

    public List<TransizioneYaml> getTransizioni() {
        return transizioni;
    }

    public void setTransizioni(List<TransizioneYaml> transizioni) {
        this.transizioni = transizioni;
    }

    /**
     * Classe interna per mappare le transizioni dal file YAML
     */
    public static class TransizioneYaml {
        private String statoPartenza;
        private String statoArrivo;
        private String ruoloRichiesto;
        private String descrizione;

        public String getStatoPartenza() {
            return statoPartenza;
        }

        public void setStatoPartenza(String statoPartenza) {
            this.statoPartenza = statoPartenza;
        }

        public String getStatoArrivo() {
            return statoArrivo;
        }

        public void setStatoArrivo(String statoArrivo) {
            this.statoArrivo = statoArrivo;
        }

        public String getRuoloRichiesto() {
            return ruoloRichiesto;
        }

        public void setRuoloRichiesto(String ruoloRichiesto) {
            this.ruoloRichiesto = ruoloRichiesto;
        }

        public String getDescrizione() {
            return descrizione;
        }

        public void setDescrizione(String descrizione) {
            this.descrizione = descrizione;
        }
    }
}
