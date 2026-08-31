package it.alnao.springbootexample.api.dto;

import it.alnao.springbootexample.api.dto.auth.LoginRequest;
import it.alnao.springbootexample.api.dto.auth.UserProfileResponse;
import it.alnao.springbootexample.core.domain.auth.AccountType;
import it.alnao.springbootexample.core.domain.auth.User;
import it.alnao.springbootexample.core.domain.auth.UserProvider;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Completa la copertura degli accessor dei DTO gia' parzialmente verificati altrove.
 */
class ApiDtoAccessorTest {

    // ---------- ErrorResponse ----------

    @Test
    void errorResponse_singleArgConstructorLeavesTheCodeNull() {
        ErrorResponse r = new ErrorResponse("qualcosa e' andato storto");
        assertEquals("qualcosa e' andato storto", r.getMessage());
        assertNull(r.getErrorCode());
    }

    @Test
    void errorResponse_twoArgsConstructorSetsBoth() {
        ErrorResponse r = new ErrorResponse("bloccata", "ANNOTATION_LOCKED");
        assertEquals("bloccata", r.getMessage());
        assertEquals("ANNOTATION_LOCKED", r.getErrorCode());
    }

    @Test
    void errorResponse_settersAndGettersWork() {
        ErrorResponse r = new ErrorResponse("iniziale");
        r.setMessage("aggiornato");
        r.setErrorCode("CODE");
        assertEquals("aggiornato", r.getMessage());
        assertEquals("CODE", r.getErrorCode());
    }

    // ---------- AggiornaAnnotazioneRequest ----------

    @Test
    void aggiornaAnnotazioneRequest_optionalFieldsAreAccessible() {
        UUID id = UUID.randomUUID();
        AggiornaAnnotazioneRequest r = new AggiornaAnnotazioneRequest(id, "valore", "descr", "mario");
        assertEquals(id, r.getId());
        assertEquals("valore", r.getValoreNota());
        assertEquals("descr", r.getDescrizione());
        assertEquals("mario", r.getUtente());

        r.setCategoria("lavoro");
        r.setTags("a,b");
        r.setPubblica(true);
        r.setPriorita(5);
        assertEquals("lavoro", r.getCategoria());
        assertEquals("a,b", r.getTags());
        assertTrue(r.getPubblica());
        assertEquals(5, r.getPriorita());
    }

    // ---------- CreaAnnotazioneRequest ----------

    @Test
    void creaAnnotazioneRequest_defaultsArePrivateAndLowPriority() {
        CreaAnnotazioneRequest r = new CreaAnnotazioneRequest();
        assertFalse(r.getPubblica());
        assertEquals(1, r.getPriorita());
    }

    @Test
    void creaAnnotazioneRequest_optionalFieldsAreAccessible() {
        CreaAnnotazioneRequest r = new CreaAnnotazioneRequest("valore", "descr", "mario");
        r.setCategoria("casa");
        r.setTags("x,y");
        r.setPubblica(true);
        r.setPriorita(3);
        assertEquals("casa", r.getCategoria());
        assertEquals("x,y", r.getTags());
        assertTrue(r.getPubblica());
        assertEquals(3, r.getPriorita());
    }

    // ---------- LoginRequest ----------

    @Test
    void loginRequest_accessorsWork() {
        LoginRequest r = new LoginRequest("mario", "secret");
        assertEquals("mario", r.getUsername());
        assertEquals("secret", r.getPassword());

        r.setUsername("luigi");
        r.setPassword("altra");
        assertEquals("luigi", r.getUsername());
        assertEquals("altra", r.getPassword());
    }

    @Test
    void loginRequest_defaultConstructorLeavesFieldsNull() {
        LoginRequest r = new LoginRequest();
        assertNull(r.getUsername());
        assertNull(r.getPassword());
    }

    @Test
    void loginRequest_toStringDoesNotLeakThePassword() {
        String text = new LoginRequest("mario", "super-segreta").toString();
        assertTrue(text.contains("mario"));
        assertFalse(text.contains("super-segreta"));
    }

    // ---------- UserProfileResponse ----------

    @Test
    void userProfileResponse_fromCopiesTheUserAndItsProviders() {
        User user = new User();
        user.setId("u-1");
        user.setUsername("mario");
        user.setEmail("mario@test.it");
        user.setFirstName("Mario");
        user.setLastName("Rossi");
        user.setAvatarUrl("http://avatar");
        user.setAccountType(AccountType.GOOGLE);
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.setCreatedAt(LocalDateTime.of(2026, 1, 1, 0, 0));
        user.setLastLogin(LocalDateTime.of(2026, 6, 1, 0, 0));
        user.setProviders(List.of(new UserProvider("u-1", "google", "g-1")));

        UserProfileResponse r = UserProfileResponse.from(user);

        assertEquals("u-1", r.getId());
        assertEquals("mario", r.getUsername());
        assertEquals("mario@test.it", r.getEmail());
        assertEquals("Mario", r.getFirstName());
        assertEquals("Rossi", r.getLastName());
        assertEquals("http://avatar", r.getAvatarUrl());
        assertEquals(AccountType.GOOGLE, r.getAccountType());
        assertEquals(List.of("google"), r.getLinkedProviders());
        assertTrue(r.isEnabled());
        assertTrue(r.isEmailVerified());
        assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), r.getCreatedAt());
        assertEquals(LocalDateTime.of(2026, 6, 1, 0, 0), r.getLastLogin());
    }

    @Test
    void userProfileResponse_fromAUserWithoutProvidersGivesAnEmptyList() {
        User user = new User();
        user.setId("u-2");
        user.setAccountType(AccountType.LOCAL);
        UserProfileResponse r = UserProfileResponse.from(user);
        assertTrue(r.getLinkedProviders().isEmpty());
    }
}
