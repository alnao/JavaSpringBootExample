package it.alnao.esempio05.controller;

import it.alnao.esempio05.service.LoginService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/login")
@Tag(name = "Login", description = "API per l'autenticazione degli utenti") // Aggiunge un tag per raggruppare gli endpoint nella UI di Swagger
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @Operation(summary = "Effettua il login di un utente",
               description = "Autentica un utente fornendo nome utente e password.") // Descrizione dell'operazione
    @ApiResponses(value = { // Possibili risposte dell'API
            @ApiResponse(responseCode = "200", description = "Login riuscito"),
            @ApiResponse(responseCode = "401", description = "Credenziali non valide")
    })
    @PostMapping
    public ResponseEntity<String> login(
            @Parameter(description = "Nome utente per il login", required = true) // Descrizione del parametro 'nome'
            @RequestParam String nome,
            @Parameter(description = "Password dell'utente", required = true) // Descrizione del parametro 'password'
            @RequestParam String password) {
        boolean result = loginService.login(nome, password);
        if (result) {
            return ResponseEntity.ok("Login riuscito");
        } else {
            return ResponseEntity.status(401).body("Login fallito");
        }
    }

}
