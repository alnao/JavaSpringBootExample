package it.alnao.springbootexample.javafx;

import it.alnao.springbootexample.javafx.config.JavaFxConfig;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Applicazione JavaFX integrata con Spring Boot
 */
public class JavaFxApplication extends Application {
    
    private ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        String[] args = getParameters().getRaw().toArray(new String[0]);
        this.springContext = new SpringApplicationBuilder()
                .sources(JavaFxConfig.class)
                .run(args);
    }

    @Override
    public void start(Stage primaryStage) {
        springContext.publishEvent(new StageReadyEvent(primaryStage));
    }

    @Override
    public void stop() {
        springContext.close();
        Platform.exit();
    }

    /**
     * Evento pubblicato quando lo Stage principale è pronto
     */
    public static class StageReadyEvent extends ApplicationEvent {
        public StageReadyEvent(Stage stage) {
            super(stage);
        }
        
        public Stage getStage() {
            return (Stage) getSource();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
