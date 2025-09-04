package it.alnao.springbootexample.api.dto.auth;

/**
 * DTO per le informazioni sui provider OAuth2 disponibili.
 */
public class OAuth2ProviderInfo {
    private String id;
    private String name;
    private String authUrl;
    private boolean available;

    // Constructors
    public OAuth2ProviderInfo() {}

    public OAuth2ProviderInfo(String id, String name, String authUrl) {
        this.id = id;
        this.name = name;
        this.authUrl = authUrl;
        this.available = true;
    }

    public OAuth2ProviderInfo(String id, String name, String authUrl, boolean available) {
        this.id = id;
        this.name = name;
        this.authUrl = authUrl;
        this.available = available;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthUrl() {
        return authUrl;
    }

    public void setAuthUrl(String authUrl) {
        this.authUrl = authUrl;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return "OAuth2ProviderInfo{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", authUrl='" + authUrl + '\'' +
                ", available=" + available +
                '}';
    }
}
