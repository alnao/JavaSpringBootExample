package it.alnao.springbootexample.sqlite.repository;

import it.alnao.springbootexample.sqlite.entity.AnnotazioneSQLiteEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Profile("sqlite")
public interface AnnotazioneSQLiteRepository extends JpaRepository<AnnotazioneSQLiteEntity, String> {
    @Query("SELECT a FROM AnnotazioneSQLiteEntity a WHERE a.id = :id")
    List<AnnotazioneSQLiteEntity> findByIdValue(@Param("id") String id);
}
