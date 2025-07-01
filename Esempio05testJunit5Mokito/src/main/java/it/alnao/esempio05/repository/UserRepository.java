package it.alnao.esempio05.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import it.alnao.esempio05.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByNome(String nome);
}