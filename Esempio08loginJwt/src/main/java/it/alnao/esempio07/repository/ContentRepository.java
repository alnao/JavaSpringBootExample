package it.alnao.esempio07.repository;

import it.alnao.esempio07.entity.Content;
import it.alnao.esempio07.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {
    
    List<Content> findByAutoreOrderByDataCreazioneDesc(User autore);
    
    List<Content> findByPubblicatoTrueOrderByDataCreazioneDesc();
    
    List<Content> findByAutoreAndPubblicatoOrderByDataCreazioneDesc(User autore, Boolean pubblicato);
    
    @Query("SELECT c FROM Content c WHERE c.pubblicato = true AND " +
           "(LOWER(c.titolo) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.descrizione) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Content> searchPublishedContents(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT c FROM Content c WHERE c.autore = :autore AND " +
           "(LOWER(c.titolo) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.descrizione) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Content> searchContentsByAuthor(@Param("autore") User autore, @Param("searchTerm") String searchTerm);
    
    @Query("SELECT COUNT(c) FROM Content c WHERE c.autore = :autore")
    long countContentsByAuthor(@Param("autore") User autore);
    
    @Query("SELECT COUNT(c) FROM Content c WHERE c.pubblicato = true")
    long countPublishedContents();
    
    List<Content> findByDataCreazioneBetween(LocalDateTime start, LocalDateTime end);
}