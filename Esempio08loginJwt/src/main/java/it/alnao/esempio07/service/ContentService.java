package it.alnao.esempio07.service;

import it.alnao.esempio07.dto.ContentDto;
import it.alnao.esempio07.entity.Content;
import it.alnao.esempio07.entity.User;
import it.alnao.esempio07.repository.ContentRepository;
import it.alnao.esempio07.repository.UserRepository;
import it.alnao.esempio07.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentService {
    
    private final ContentRepository contentRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public ContentDto createContent(ContentDto contentDto, String userEmail) {
        log.info("Creazione nuovo contenuto per utente: {}", userEmail);
        
        User autore = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException("Utente non trovato: " + userEmail));
        
        Content content = Content.builder()
                .titolo(contentDto.getTitolo())
                .descrizione(contentDto.getDescrizione())
                .contenuto(contentDto.getContenuto())
                .pubblicato(contentDto.getPubblicato() != null ? contentDto.getPubblicato() : false)
                .autore(autore)
                .dataCreazione(LocalDateTime.now())
                .build();
        
        Content savedContent = contentRepository.save(content);
        log.info("Contenuto creato con successo: {}", savedContent.getId());
        
        return convertToDto(savedContent);
    }
    
    @Transactional(readOnly = true)
    public List<ContentDto> getAllContents() {
        log.info("Recupero tutti i contenuti");
        return contentRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ContentDto> getPublishedContents() {
        log.info("Recupero contenuti pubblicati");
        return contentRepository.findByPubblicatoTrueOrderByDataCreazioneDesc().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ContentDto> getContentsByUser(String userEmail) {
        log.info("Recupero contenuti per utente: {}", userEmail);
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException("Utente non trovato: " + userEmail));
        
        return contentRepository.findByAutoreOrderByDataCreazioneDesc(user).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public ContentDto getContentById(Long id) {
        log.info("Recupero contenuto con ID: {}", id);
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new CustomException("Contenuto non trovato con ID: " + id));
        return convertToDto(content);
    }
    
    @Transactional
    public ContentDto updateContent(Long id, ContentDto contentDto, String userEmail) {
        log.info("Aggiornamento contenuto con ID: {} per utente: {}", id, userEmail);
        
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new CustomException("Contenuto non trovato con ID: " + id));
        
        // Verifica che l'utente sia l'autore del contenuto
        if (!content.getAutore().getEmail().equals(userEmail)) {
            throw new CustomException("Non hai i permessi per modificare questo contenuto");
        }
        
        content.setTitolo(contentDto.getTitolo());
        content.setDescrizione(contentDto.getDescrizione());
        content.setContenuto(contentDto.getContenuto());
        content.setPubblicato(contentDto.getPubblicato() != null ? contentDto.getPubblicato() : content.getPubblicato());
        content.setDataModifica(LocalDateTime.now());
        
        Content updatedContent = contentRepository.save(content);
        log.info("Contenuto aggiornato con successo: {}", updatedContent.getId());
        
        return convertToDto(updatedContent);
    }
    
    @Transactional
    public void deleteContent(Long id, String userEmail) {
        log.info("Eliminazione contenuto con ID: {} per utente: {}", id, userEmail);
        
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new CustomException("Contenuto non trovato con ID: " + id));
        
        // Verifica che l'utente sia l'autore del contenuto
        if (!content.getAutore().getEmail().equals(userEmail)) {
            throw new CustomException("Non hai i permessi per eliminare questo contenuto");
        }
        
        contentRepository.delete(content);
        log.info("Contenuto eliminato con successo: {}", id);
    }
    
    @Transactional(readOnly = true)
    public List<ContentDto> searchContents(String searchTerm) {
        log.info("Ricerca contenuti con termine: {}", searchTerm);
        return contentRepository.searchPublishedContents(searchTerm).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ContentDto> searchContentsByUser(String userEmail, String searchTerm) {
        log.info("Ricerca contenuti per utente: {} con termine: {}", userEmail, searchTerm);
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException("Utente non trovato: " + userEmail));
        
        return contentRepository.searchContentsByAuthor(user, searchTerm).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    private ContentDto convertToDto(Content content) {
        return ContentDto.builder()
                .id(content.getId())
                .titolo(content.getTitolo())
                .descrizione(content.getDescrizione())
                .contenuto(content.getContenuto())
                .pubblicato(content.getPubblicato())
                .dataCreazione(content.getDataCreazione())
                .dataModifica(content.getDataModifica())
                .autoreId(content.getAutore().getId())
                .autoreNome(content.getAutore().getNome())
                .autoreCognome(content.getAutore().getCognome())
                .autoreEmail(content.getAutore().getEmail())
                .build();
    }
}