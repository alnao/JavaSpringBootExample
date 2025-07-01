package it.alnao.esempio05.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.alnao.esempio05.repository.UserRepository;

@Service
public class LoginService {

    @Autowired
    private UserRepository userRepository;

    public boolean login(String nome, String password) {
        return userRepository.findByNome(nome)
                .map(user -> user.getPassword().equals(password))
                .orElse(false);
    }
}
