package it.alnao.esempio06.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import it.alnao.esempio06.entity.Content;

public interface ContentRepository extends JpaRepository<Content, Long> {
    Content findTopByOrderByIdDesc();
}