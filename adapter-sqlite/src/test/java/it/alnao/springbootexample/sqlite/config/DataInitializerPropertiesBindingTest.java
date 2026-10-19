package it.alnao.springbootexample.sqlite.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Documenta come il binder di Spring Boot risolve il placeholder della password quando la
 * variabile d'ambiente non esiste: e' il motivo per cui application-sqlite.yml usa il default
 * vuoto esplicito "${APP_USER_..._PASSWORD:}" (design D1 della change secret-sqlite).
 */
class DataInitializerPropertiesBindingTest {

    private static final String VARIABILE_ASSENTE = "APP_USER_VARIABILE_CHE_NON_ESISTE_PASSWORD";

    private DataInitializerProperties bind(String password) {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("test", Map.of(
                "app.init.users.enabled", "true",
                "app.init.users.default-users[0].username", "admin",
                "app.init.users.default-users[0].password", password)));
        return Binder.get(environment).bind("app.init", DataInitializerProperties.class).get();
    }

    @Test
    void placeholderSenzaDefault_lasciaIlTestoLetterale() {
        DataInitializerProperties properties = bind("${" + VARIABILE_ASSENTE + "}");
        assertEquals("${" + VARIABILE_ASSENTE + "}", properties.getUsers().getDefaultUsers().get(0).getPassword());
    }

    @Test
    void placeholderConDefaultVuoto_produceUnaPasswordVuota() {
        DataInitializerProperties properties = bind("${" + VARIABILE_ASSENTE + ":}");
        assertEquals("", properties.getUsers().getDefaultUsers().get(0).getPassword());
    }
}
