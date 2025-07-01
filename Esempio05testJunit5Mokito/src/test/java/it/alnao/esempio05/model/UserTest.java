package it.alnao.esempio05.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testCostruttoreConArgs() {
        User user = new User(1L, "mario", "1234");

        assertEquals(1L, user.getId());
        assertEquals("mario", user.getNome());
        assertEquals("1234", user.getPassword());
    }

    @Test
    void testSetterGetter() {
        User user = new User();
        user.setId(2L);
        user.setNome("giulia");
        user.setPassword("abcd");

        assertEquals(2L, user.getId());
        assertEquals("giulia", user.getNome());
        assertEquals("abcd", user.getPassword());
    }
}
