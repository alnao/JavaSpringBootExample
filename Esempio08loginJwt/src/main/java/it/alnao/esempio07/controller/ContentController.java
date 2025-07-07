package it.alnao.esempio07.controller;

import it.alnao.esempio07.dto.ContentDto;
import it.alnao.esempio07.service.ContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ContentController {
    
    private final ContentService contentService;
    
    @GetMapping
    public ResponseEntity<List<ContentDto>> getAllContents() {
        log.info("Richiesta di recupero tutti i contenuti");
        List<ContentDto> contents = contentService.getAllContents();
        return ResponseEntity.ok(contents);
    }
    
    @GetMapping("/published")
    public ResponseEntity<List<ContentDto>> getPublishedContents() {
        log.info("Richiesta di recupero contenuti pubblicati");
        List<ContentDto> contents = contentService.getPublishedContents();
        return ResponseEntity.ok(contents);
    }
    
    @GetMapping("/my")
    public ResponseEntity<List<ContentDto>> getMyContents(Authentication authentication) {
        log.info("Richiesta di recupero contenuti dell'utente corrente");
        List<ContentDto> contents = contentService.getContentsByUser(authentication.getName());
        return ResponseEntity.ok(contents);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ContentDto> getContentById(@PathVariable Long id) {
        log.info("Richiesta di recupero contenuto con ID: {}", id);
        ContentDto content = contentService.getContentById(id);
        return ResponseEntity.ok(content);
    }
    
    @PostMapping
    public ResponseEntity<ContentDto> createContent(@Valid @RequestBody ContentDto contentDto,
                                                     Authentication authentication) {
        log.info("Richiesta di creazione contenuto");
        ContentDto createdContent = contentService.createContent(contentDto, authentication.getName());
        return ResponseEntity.ok(createdContent);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ContentDto> updateContent(@PathVariable Long id,
                                                     @Valid @RequestBody ContentDto contentDto,
                                                     Authentication authentication) {
        log.info("Richiesta di aggiornamento contenuto con ID: {}", id);
        ContentDto updatedContent = contentService.updateContent(id, contentDto, authentication.getName());
        return ResponseEntity.ok(updatedContent);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContent(@PathVariable Long id,
                                               Authentication authentication) {
        log.info("Richiesta di eliminazione contenuto con ID: {}", id);
        contentService.deleteContent(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<ContentDto>> searchContents(@RequestParam String q) {
        log.info("Richiesta di ricerca contenuti con termine: {}", q);
        List<ContentDto> contents = contentService.searchContents(q);
        return ResponseEntity.ok(contents);
    }
    
    @GetMapping("/my/search")
    public ResponseEntity<List<ContentDto>> searchMyContents(@RequestParam String q,
                                                             Authentication authentication) {
        log.info("Richiesta di ricerca contenuti dell'utente corrente con termine: {}", q);
        List<ContentDto> contents = contentService.searchContentsByUser(authentication.getName(), q);
        return ResponseEntity.ok(contents);
    }
}