package it.alnao.esempio07.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
//@SecurityRequirement(name = "basicAuth")
public class HelloController {

    @GetMapping("/")
    public String publicEndpoint() {
        return "Benvenuto! Questo endpoint è pubblico.";
    }

    @GetMapping("/hello")
    public String sayHello() {
        return "Ciao, sei autenticato!";
    }

    @GetMapping("/admin/hello")
    public String adminHello() {
        return "Ciao Admin! Accesso autorizzato.";
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/home")
    public String getHome() {
        return "Benvenuto nella tua area riservata, utente!";
    }
}
