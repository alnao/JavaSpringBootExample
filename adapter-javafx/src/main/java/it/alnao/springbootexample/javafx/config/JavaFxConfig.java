package it.alnao.springbootexample.javafx.config;

import it.alnao.springbootexample.javafx.JavaFxApplication;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.io.IOException;

/**
 * Configurazione Spring Boot per l'applicazione JavaFX
 */
@SpringBootApplication(exclude = {
    org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration.class,
    org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
})
@ComponentScan(basePackages = {
    "it.alnao.springbootexample.javafx",
    "it.alnao.springbootexample.core.service",
    "it.alnao.springbootexample.core.config",
    "it.alnao.springbootexample.sqlite"
},
excludeFilters = @ComponentScan.Filter(
    type = org.springframework.context.annotation.FilterType.REGEX,
    pattern = "it\\.alnao\\.springbootexample\\.core\\.security\\..*"
))
public class JavaFxConfig {

    @Value("${spring.application.name:Gestione Annotazioni}")
    private String applicationTitle;

    private final ConfigurableApplicationContext springContext;

    public JavaFxConfig(ConfigurableApplicationContext springContext) {
        this.springContext = springContext;
    }

    @Bean
    public ApplicationListener<JavaFxApplication.StageReadyEvent> stageReadyEventListener() {
        return event -> {
            try {
                Stage stage = event.getStage();
                
                // Carica il file FXML per la schermata di login
                FXMLLoader fxmlLoader = new FXMLLoader(
                    getClass().getResource("/fxml/login.fxml")
                );
                fxmlLoader.setControllerFactory(springContext::getBean);
                
                Parent root = fxmlLoader.load();
                
                Scene scene = new Scene(root, 400, 300);
                stage.setScene(scene);
                stage.setTitle(applicationTitle);
                stage.setResizable(false);
                stage.show();
                
            } catch (IOException e) {
                throw new RuntimeException("Errore nel caricamento dell'interfaccia", e);
            }
        };
    }
}
