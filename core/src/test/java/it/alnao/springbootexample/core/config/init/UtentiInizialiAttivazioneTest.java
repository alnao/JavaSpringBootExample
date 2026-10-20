package it.alnao.springbootexample.core.config.init;

import it.alnao.springbootexample.core.repository.auth.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * L'initializer degli utenti iniziali esiste solo con app.init.users.enabled=true: nei profili senza
 * creazione utenti (aws, azure) non viene creato e non richiede UserRepository, che azure non implementa.
 */
class UtentiInizialiAttivazioneTest {

    // ConfigurationPropertiesAutoConfiguration attiva il binding di app.init.*, come nell'applicazione vera
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
            .withUserConfiguration(UtentiInizialiProperties.class, UtentiInizialiInitializer.class);

    @Test
    void senzaProprieta_beanAssenteENessunUserRepositoryRichiesto() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(UtentiInizialiInitializer.class);
        });
    }

    @Test
    void conEnabledFalse_beanAssente() {
        runner.withPropertyValues("app.init.users.enabled=false")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).doesNotHaveBean(UtentiInizialiInitializer.class);
                });
    }

    @Test
    void conEnabledTrue_beanPresenteConLaListaConfigurata() {
        runner.withPropertyValues(
                        "app.init.users.enabled=true",
                        "app.init.users.default-users[0].username=admin",
                        "app.init.users.default-users[0].password=Prova-123",
                        "app.init.users.default-users[0].role=ADMIN")
                .withBean(UserRepository.class, () -> mock(UserRepository.class))
                .withBean(PasswordEncoder.class, () -> mock(PasswordEncoder.class))
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(UtentiInizialiInitializer.class);
                    assertThat(context.getBean(UtentiInizialiProperties.class).getUsers().getDefaultUsers())
                            .hasSize(1);
                });
    }

    @Test
    void conEnabledTrueEPasswordMancante_contestoNonParte() {
        runner.withPropertyValues(
                        "app.init.users.enabled=true",
                        "app.init.users.default-users[0].username=moderatore",
                        "app.init.users.default-users[0].password=")
                .withBean(UserRepository.class, () -> mock(UserRepository.class))
                .withBean(PasswordEncoder.class, () -> mock(PasswordEncoder.class))
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseMessage("Credenziale mancante: APP_USER_MODERATORE_PASSWORD");
                });
    }
}
