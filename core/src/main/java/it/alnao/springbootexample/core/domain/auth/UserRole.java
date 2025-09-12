package it.alnao.springbootexample.core.domain.auth;

/**
 * Ruoli degli utenti nel sistema.
 */
public enum UserRole {
    USER("Utente normale"),
    ADMIN("Amministratore"),
    MODERATOR("Moderatore"),
    SYSTEM("Sistema automatico");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getRoleName() {
        return this.name(); //ex "ROLE_" + 
    }
}
