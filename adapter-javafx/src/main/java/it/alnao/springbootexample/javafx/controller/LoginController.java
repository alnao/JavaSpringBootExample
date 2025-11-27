package it.alnao.springbootexample.javafx.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Controller per la schermata di login
 */
@Component
public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    private final ConfigurableApplicationContext springContext;

    public LoginController(ConfigurableApplicationContext springContext) {
        this.springContext = springContext;
    }

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        
        // Imposta valori di default per test
        usernameField.setText("admin");
        passwordField.setText("password");
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Username e password sono obbligatori");
            return;
        }

        // TODO: Implementare autenticazione JWT reale
        // Per ora accettiamo qualsiasi credenziale
        if (authenticateUser(username, password)) {
            openMainWindow(username);
        } else {
            showError("Credenziali non valide");
        }
    }

    private boolean authenticateUser(String username, String password) {
        // Autenticazione semplificata per test
        // TODO: Integrare con il servizio di autenticazione reale
        return true;
    }

    private void openMainWindow(String username) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/fxml/main.fxml")
            );
            fxmlLoader.setControllerFactory(springContext::getBean);
            
            Parent root = fxmlLoader.load();
            
            // Passa lo username al controller principale
            MainController mainController = fxmlLoader.getController();
            mainController.setCurrentUser(username);
            
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root, 1024, 768);
            stage.setScene(scene);
            stage.setTitle("Gestione Annotazioni - " + username);
            stage.setResizable(true);
            stage.centerOnScreen();
            
        } catch (Exception e) {
            showError("Errore nell'apertura della finestra principale: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.close();
    }
}
