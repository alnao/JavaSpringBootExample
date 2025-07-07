package it.alnao.esempio07.repository;

import it.alnao.esempio07.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    List<User> findByFlagAttivoTrue();
    
    @Query("SELECT u FROM User u WHERE u.flagAttivo = true AND " +
           "(LOWER(u.nome) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.cognome) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<User> searchActiveUsers(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.flagAttivo = true")
    long countActiveUsers();
    
    @Query("SELECT u FROM User u WHERE u.dataUltimoAccesso IS NULL OR u.dataUltimoAccesso < :date")
    List<User> findUsersNotLoggedInSince(@Param("date") LocalDateTime date);
}