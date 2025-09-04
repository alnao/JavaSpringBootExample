package it.alnao.springbootexample.port.domain.auth;

/**
 * Ruoli degli utenti nel sistema.
 */
public enum UserRole {
    USER("Utente normale"),
    ADMIN("Amministratore"),
    MODERATOR("Moderatore");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getRoleName() {
        return "ROLE_" + this.name();
    }
}
