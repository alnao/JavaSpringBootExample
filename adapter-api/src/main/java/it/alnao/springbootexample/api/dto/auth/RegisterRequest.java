package it.alnao.springbootexample.api.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO per la richiesta di registrazione locale.
 */
public class RegisterRequest {
    
    @NotBlank(message = "Username è obbligatorio")
    @Size(min = 3, max = 50, message = "Username deve essere tra 3 e 50 caratteri")
    private String username;
    
    @NotBlank(message = "Email è obbligatoria")
    @Email(message = "Email deve essere valida")
    @Size(max = 100, message = "Email deve essere massimo 100 caratteri")
    private String email;
    
    @NotBlank(message = "Password è obbligatoria")
    @Size(min = 6, max = 100, message = "Password deve essere tra 6 e 100 caratteri")
    private String password;
    
    @Size(max = 50, message = "Nome deve essere massimo 50 caratteri")
    private String firstName;
    
    @Size(max = 50, message = "Cognome deve essere massimo 50 caratteri")
    private String lastName;

    // Constructors
    public RegisterRequest() {}

    public RegisterRequest(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        return "RegisterRequest{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }
}
