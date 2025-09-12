package it.alnao.springbootexample.core.portService;

import it.alnao.springbootexample.core.domain.auth.User;
import it.alnao.springbootexample.core.domain.auth.UserRole;
import it.alnao.springbootexample.core.domain.StatoAnnotazione;
import it.alnao.springbootexample.core.domain.TransizioneStato;
import it.alnao.springbootexample.core.service.AnnotazioneService;
import it.alnao.springbootexample.core.service.AnnotazioneStoricoStatiService;
import it.alnao.springbootexample.core.service.ValidatoreTransizioniStatoService;
import it.alnao.springbootexample.core.service.auth.UserService;
import it.alnao.springbootexample.core.domain.AnnotazioneCompleta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AnnotazioniPortServiceImpl implements AnnotazioniPortService {
    
    @Autowired
    private AnnotazioneService annotazioneService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ValidatoreTransizioniStatoService validatoreTransizioniStatoService;
    
    @Autowired
    private AnnotazioneStoricoStatiService annotazioneStoricoStatiService;

        public AnnotazioneCompleta creaAnnotazione(AnnotazioneCompleta annotazione, String utente) {
            AnnotazioneCompleta annotazioneCompleta = annotazioneService.creaAnnotazione(
                    annotazione.getAnnotazione().getValoreNota(),
                    annotazione.getMetadata().getDescrizione(),
                    utente
            );
            if (annotazione.getMetadata().getCategoria() != null) {
                annotazioneService.impostaCategoria(annotazioneCompleta.getId(), annotazione.getMetadata().getCategoria(), utente);
            }
            if (annotazione.getMetadata().getTags() != null) {
                annotazioneService.impostaTags(annotazioneCompleta.getId(), annotazione.getMetadata().getTags(), utente);
            }
            if (annotazione.getMetadata().getPubblica() != null) {
                annotazioneService.impostaVisibilitaPubblica(annotazioneCompleta.getId(), annotazione.getMetadata().getPubblica(), utente);
            }
            if (annotazione.getMetadata().getPriorita() != null) {
                annotazioneService.impostaPriorita(annotazioneCompleta.getId(), annotazione.getMetadata().getPriorita(), utente);
            }
            return annotazioneService.trovaPerID(annotazioneCompleta.getId()).orElse(null);
        }

    public AnnotazioneCompleta aggiornaAnnotazione(AnnotazioneCompleta annotazione, String utente) {
        // 1. Recupera il ruolo dell'utente
        User user = userService.findByUsername(utente).orElseThrow(() -> new IllegalArgumentException("Utente non trovato: " + utente));
        UserRole ruoloUtente = user.getRole();
        // 2. Verifica se il ruolo abilita a fare quel cambio stato
        validatoreTransizioniStatoService.validaTransizione(StatoAnnotazione.INSERITA, StatoAnnotazione.MODIFICATA, ruoloUtente);
        // 3. Recupera l'annotazione attuale per ottenere la versione
        UUID id = annotazione.getId();
        annotazioneService.aggiornaAnnotazione(
                id,
                annotazione.getAnnotazione().getValoreNota(),
                annotazione.getMetadata().getDescrizione(),
                utente
        );
        // 4. Aggiorna i metadati se presenti
        if (annotazione.getMetadata().getCategoria() != null) {
            annotazioneService.impostaCategoria(id, annotazione.getMetadata().getCategoria(), utente);
        }
        if (annotazione.getMetadata().getTags() != null) {
            annotazioneService.impostaTags(id, annotazione.getMetadata().getTags(), utente);
        }
        if (annotazione.getMetadata().getPubblica() != null) {
            annotazioneService.impostaVisibilitaPubblica(id, annotazione.getMetadata().getPubblica(), utente);
        }
        if (annotazione.getMetadata().getPriorita() != null) {
            annotazioneService.impostaPriorita(id, annotazione.getMetadata().getPriorita(), utente);
        }
        // 5. Aggiorna lo stato
        cambiaStato(id, StatoAnnotazione.INSERITA, StatoAnnotazione.MODIFICATA, utente);
        return annotazioneService.trovaPerID(id).orElse(null);
    }

    @Override
    public AnnotazioneCompleta cambiaStato(UUID id, StatoAnnotazione vecchioStato, StatoAnnotazione nuovoStato, String utente) {
        // 1. Recupera il ruolo dell'utente
        User user = userService.findByUsername(utente)
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato: " + utente));
        UserRole ruoloUtente = user.getRole();

        // 2. Verifica se il ruolo abilita a fare quel cambio stato
        validatoreTransizioniStatoService.validaTransizione(vecchioStato, nuovoStato, ruoloUtente);

        // 3. Recupera l'annotazione attuale per ottenere la versione
        Optional<AnnotazioneCompleta> annotazioneOpt = annotazioneService.trovaPerID(id);
        if (annotazioneOpt.isEmpty()) {
            throw new IllegalArgumentException("Annotazione non trovata con ID: " + id);
        }
        
        AnnotazioneCompleta annotazioneAttuale = annotazioneOpt.get();
        String versione = annotazioneAttuale.getAnnotazione().getVersioneNota();

        // 3. Inserisce riga nel AnnotazioneStoricoStati
        String notaOperazione = String.format("Cambio stato da %s a %s effettuato da %s", 
                                            vecchioStato.getValue(), nuovoStato.getValue(), utente);
        
        annotazioneStoricoStatiService.inserisciCambioStato(
            id.toString(), 
            versione, 
            nuovoStato.getValue(), 
            vecchioStato.getValue(), 
            utente, 
            notaOperazione
        );

        // 4. Update nella Metadati con il metodo cambiaStato
        return annotazioneService.cambiaStato(id, nuovoStato.getValue(), utente);
    }

    public List<AnnotazioneCompleta> trovaTutte() {
        return annotazioneService.trovaTutte();
    }

    public Optional<AnnotazioneCompleta> trovaPerID(UUID id) {
        return annotazioneService.trovaPerID(id);
    }

    public void eliminaAnnotazione(UUID id) {
        annotazioneService.eliminaAnnotazione(id);
    }

    public List<AnnotazioneCompleta> cercaPerTesto(String testo) {
        return annotazioneService.cercaPerTesto(testo);
    }

    public List<AnnotazioneCompleta> trovaPerUtente(String utente) {
        return annotazioneService.trovaPerUtente(utente);
    }

    public List<AnnotazioneCompleta> trovaPerCategoria(String categoria) {
        return annotazioneService.trovaPerCategoria(categoria);
    }

    public List<AnnotazioneCompleta> trovaPubbliche() {
        return annotazioneService.trovaPubbliche();
    }

    public List<AnnotazioneCompleta> trovaPerStato(StatoAnnotazione stato) {
        return annotazioneService.trovaPerStato(stato);
    }

    public long contaAnnotazioni() {
        return annotazioneService.contaAnnotazioni();
    }

    @Override
    public List<TransizioneStato> listaCambiamentiStati() {
        return validatoreTransizioniStatoService.getTutteLeTransizioni();
    }
}
