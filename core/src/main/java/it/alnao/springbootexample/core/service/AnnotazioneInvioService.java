package it.alnao.springbootexample.core.service;

import it.alnao.springbootexample.core.domain.AnnotazioneCompleta;
import java.util.List;

public interface AnnotazioneInvioService {
    
    /**
     * Invia le annotazioni in stato DAINVIARE
     * @return lista delle annotazioni inviate con successo
     */
    List<AnnotazioneCompleta> inviaAnnotazioni();
    
    /**
     * Verifica se il servizio di invio è abilitato
     * @return true se abilitato
     */
    boolean isEnabled();
}
