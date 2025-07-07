package it.alnao.esempio07;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void shouldReturnPublicMessageWithoutAuth() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(content().string("Benvenuto! Questo endpoint è pubblico."));
    }

    @Test
    void shouldRejectHelloWithoutAuth() throws Exception {
        mockMvc.perform(get("/hello"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "alnao", roles = "USER")
    void shouldAllowHelloForUser() throws Exception {
        mockMvc.perform(get("/hello"))
            .andExpect(status().isOk())
            .andExpect(content().string("Ciao, sei autenticato!"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldAllowAdminHello() throws Exception {
        mockMvc.perform(get("/admin/hello")) 
            .andExpect(status().isOk())
            .andExpect(content().string("Ciao Admin! Accesso autorizzato."));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void shouldRejectAdminHelloForUser() throws Exception {
        mockMvc.perform(get("/admin/hello"))
            .andExpect(status().isForbidden());
    }

    @WithMockUser(username = "user", roles = {"USER"})
    @Test
    void shouldAccessHomeWithUserRole() throws Exception {
        mockMvc.perform(get("/home"))
            .andExpect(status().isOk())
            .andExpect(content().string("Benvenuto nella tua area riservata, utente!"));
    }
}
