package it.alnao.springbootexample.core.service;

import it.alnao.springbootexample.core.domain.AnnotazioneCompleta;

import java.util.List;

public interface AnnotazioneImportService {

    /**
     * Importa annotazioni da un sistema esterno.
     * @return lista delle annotazioni importate con successo
     */
    List<AnnotazioneCompleta> importaAnnotazioni();

    /**
     * Verifica se il servizio di import è abilitato.
     */
    boolean isEnabled();
}
