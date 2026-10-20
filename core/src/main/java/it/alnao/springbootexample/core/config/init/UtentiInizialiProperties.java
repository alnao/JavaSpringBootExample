package it.alnao.springbootexample.core.config.init;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.ArrayList;

/**
 * Utenti iniziali creati all'avvio (prefisso app.init.users). La lista e' dichiarata in application.yml
 * con le password lette da APP_USER_<USERNAME>_PASSWORD; ogni profilo decide se attivare la creazione
 * con app.init.users.enabled (change openspec secret-sqlite e secret-kube).
 */
@Component
@ConfigurationProperties(prefix = "app.init")
public class UtentiInizialiProperties {

    private Users users = new Users();

    public Users getUsers() { return users; }
    public void setUsers(Users users) { this.users = users; }
    
    public static class Users {
        private boolean enabled = true;
        private List<UserConfig> defaultUsers = new ArrayList<>();
        
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        
        public List<UserConfig> getDefaultUsers() { return defaultUsers; }
        public void setDefaultUsers(List<UserConfig> defaultUsers) { this.defaultUsers = defaultUsers; }

        @Override
        public String toString() {
            return "Users{" +
                    "enabled=" + enabled +
                    ", defaultUsers=" + defaultUsers +
                    '}';
        }
    }
    
    public static class UserConfig {
        private String username;
        private String email;
        private String password;
        private String role = "USER";
        
        // Getters e setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        @Override
        public String toString() {
            return "UserConfig{" +
                    "username='" + username + '\'' +
                    ", email='" + email + '\'' +
                    ", password='****'" +
                    ", role='" + role + '\'' +
                    '}';
        }
    }
}
