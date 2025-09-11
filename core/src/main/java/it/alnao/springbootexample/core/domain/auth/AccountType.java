package it.alnao.springbootexample.core.domain.auth;

/**
 * Tipi di account supportati dal sistema.
 */
public enum AccountType {
    LOCAL("Account locale"),
    GOOGLE("Google OAuth2"),
    GITHUB("GitHub OAuth2"),
    MICROSOFT("Microsoft OAuth2"),
    FACEBOOK("Facebook OAuth2");

    private final String description;

    AccountType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static AccountType fromString(String provider) {
        return switch (provider.toLowerCase()) {
            case "google" -> GOOGLE;
            case "github" -> GITHUB;
            case "microsoft" -> MICROSOFT;
            case "facebook" -> FACEBOOK;
            default -> LOCAL;
        };
    }
}
