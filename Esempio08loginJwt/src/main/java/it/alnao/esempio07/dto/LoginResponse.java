package it.alnao.esempio07.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    
    private String token;
    private String type = "Bearer";
    private UserDto user;
    private String message;
    
    public LoginResponse(String token, UserDto user) {
        this.token = token;
        this.user = user;
        this.message = "Login effettuato con successo";
    }
}