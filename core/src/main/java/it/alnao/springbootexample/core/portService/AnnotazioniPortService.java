package it.alnao.springbootexample.core.portService;

import it.alnao.springbootexample.core.domain.AnnotazioneCompleta;
import it.alnao.springbootexample.core.domain.StatoAnnotazione;
import it.alnao.springbootexample.core.domain.TransizioneStato;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface AnnotazioniPortService {

    public AnnotazioneCompleta creaAnnotazione(AnnotazioneCompleta annotazione, String utente);

    public AnnotazioneCompleta aggiornaAnnotazione(AnnotazioneCompleta annotazione, String utente);

    /**
     * Cambia lo stato di un'annotazione verificando i permessi dell'utente
     */
    public AnnotazioneCompleta cambiaStato(UUID id, StatoAnnotazione vecchioStato, StatoAnnotazione nuovoStato, String utente);

    public List<AnnotazioneCompleta> trovaTutte();

    public Optional<AnnotazioneCompleta> trovaPerID(UUID id);

    public void eliminaAnnotazione(UUID id);

    public List<AnnotazioneCompleta> cercaPerTesto(String testo);

    public List<AnnotazioneCompleta> trovaPerUtente(String utente);

    public List<AnnotazioneCompleta> trovaPerCategoria(String categoria);

    public List<AnnotazioneCompleta> trovaPubbliche();

    public List<AnnotazioneCompleta> trovaPerStato(StatoAnnotazione stato);

    public long contaAnnotazioni();

    /**
     * Ottiene la lista di tutti i cambiamenti di stato configurati
     * @return Lista delle transizioni di stato permesse
     */
    public List<TransizioneStato> listaCambiamentiStati();
    
}
