package it.alnao.annotazioni.api.mapper;

import it.alnao.annotazioni.api.dto.AnnotazioneResponse;
import it.alnao.annotazioni.port.domain.AnnotazioneCompleta;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper per convertire tra domini e DTO
 */
public class AnnotazioneMapper {
    
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
