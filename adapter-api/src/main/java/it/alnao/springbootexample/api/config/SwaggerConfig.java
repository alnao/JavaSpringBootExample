package it.alnao.springbootexample.api.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI getGestioneAnnotazioniOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Sistema di Gestione Annotazioni API")
                        .description("API per la gestione delle annotazioni.")
                        .version("v1.0.0")
                        .license(new License().name("GPL v3").url("https://www.gnu.org/licenses/gpl-3.0")))
                .externalDocs(new ExternalDocumentation()
                        .description("Documentazione progetto e repository")
                        .url("https://www.alnao.it/"));
    }
}
