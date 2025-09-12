package it.alnao.springbootexample.api.mapper;

import it.alnao.springbootexample.api.dto.AnnotazioneResponse;
import it.alnao.springbootexample.core.domain.AnnotazioneCompleta;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper per convertire tra domini e DTO
 */
public class AnnotazioneMapper {
    /**
     * Converte CreaAnnotazioneRequest in AnnotazioneCompleta
     */
    public static AnnotazioneCompleta fromCreateRequest(it.alnao.springbootexample.api.dto.CreaAnnotazioneRequest request) {
        AnnotazioneCompleta ac = new AnnotazioneCompleta();
        // Popola i dati base
        it.alnao.springbootexample.core.domain.Annotazione annotazione = new it.alnao.springbootexample.core.domain.Annotazione();
        annotazione.setValoreNota(request.getValoreNota());
        ac.setAnnotazione(annotazione);
        it.alnao.springbootexample.core.domain.AnnotazioneMetadata metadata = new it.alnao.springbootexample.core.domain.AnnotazioneMetadata();
        metadata.setDescrizione(request.getDescrizione());
        metadata.setUtenteCreazione(request.getUtente());
        metadata.setCategoria(request.getCategoria());
        metadata.setTags(request.getTags());
        metadata.setPubblica(request.getPubblica());
        metadata.setPriorita(request.getPriorita());
        ac.setMetadata(metadata);
        return ac;
    }

    /**
     * Converte AggiornaAnnotazioneRequest in AnnotazioneCompleta
     */
    public static AnnotazioneCompleta fromUpdateRequest(it.alnao.springbootexample.api.dto.AggiornaAnnotazioneRequest request) {
        AnnotazioneCompleta ac = new AnnotazioneCompleta();
        it.alnao.springbootexample.core.domain.Annotazione annotazione = new it.alnao.springbootexample.core.domain.Annotazione();
        annotazione.setId(request.getId());
        annotazione.setValoreNota(request.getValoreNota());
        ac.setAnnotazione(annotazione);
        it.alnao.springbootexample.core.domain.AnnotazioneMetadata metadata = new it.alnao.springbootexample.core.domain.AnnotazioneMetadata();
        metadata.setDescrizione(request.getDescrizione());
        metadata.setUtenteCreazione(request.getUtente());
        metadata.setCategoria(request.getCategoria());
        metadata.setTags(request.getTags());
        metadata.setPubblica(request.getPubblica());
        metadata.setPriorita(request.getPriorita());
        ac.setMetadata(metadata);
        return ac;
    }
    
    /**
     * Converte un'annotazione completa in response DTO
     */
    public static AnnotazioneResponse toResponse(AnnotazioneCompleta annotazioneCompleta) {
        if (annotazioneCompleta == null) {
            return null;
        }
        
        AnnotazioneResponse response = new AnnotazioneResponse();
        
        // Dati dall'annotazione (NoSQL)
        if (annotazioneCompleta.getAnnotazione() != null) {
            response.setId(annotazioneCompleta.getAnnotazione().getId());
            response.setVersioneNota(annotazioneCompleta.getAnnotazione().getVersioneNota());
            response.setValoreNota(annotazioneCompleta.getAnnotazione().getValoreNota());
        }
        
        // Dati dai metadati (SQL)
        if (annotazioneCompleta.getMetadata() != null) {
            response.setDescrizione(annotazioneCompleta.getMetadata().getDescrizione());
            response.setUtenteCreazione(annotazioneCompleta.getMetadata().getUtenteCreazione());
            response.setDataInserimento(annotazioneCompleta.getMetadata().getDataInserimento());
            response.setDataUltimaModifica(annotazioneCompleta.getMetadata().getDataUltimaModifica());
            response.setUtenteUltimaModifica(annotazioneCompleta.getMetadata().getUtenteUltimaModifica());
            response.setCategoria(annotazioneCompleta.getMetadata().getCategoria());
            response.setTags(annotazioneCompleta.getMetadata().getTags());
            response.setPubblica(annotazioneCompleta.getMetadata().getPubblica());
            response.setPriorita(annotazioneCompleta.getMetadata().getPriorita());
            response.setStato(annotazioneCompleta.getMetadata().getStato());
        }
        
        return response;
    }
    
    /**
     * Converte una lista di annotazioni complete in lista di response DTO
     */
    public static List<AnnotazioneResponse> toResponseList(List<AnnotazioneCompleta> annotazioni) {
        if (annotazioni == null) {
            return java.util.Collections.emptyList();
        }
        return annotazioni.stream()
                .map(AnnotazioneMapper::toResponse)
                .collect(Collectors.toList());
    }
}
