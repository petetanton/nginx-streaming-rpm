package uk.tanton.streaming.live.security;

import org.junit.Test;

import java.security.NoSuchAlgorithmException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;

public class PasswordUtilsTest {

    private static final String EXAMPLE_SALT = "yADIQwFvpLAfRkat0XkUHwRJjFxBJOPD";

    @Test
    public void itGeneratesAGoodSalt() throws NoSuchAlgorithmException {
        final String salt1 = PasswordUtils.getSalt();
        final String salt2 = PasswordUtils.getSalt();
        final String salt3 = PasswordUtils.getSalt();

        assertNotSame(salt1, salt2);
        assertNotSame(salt1, salt3);
        assertNotSame(salt2, salt3);

        assertEquals(32, salt1.length());
        assertEquals(32, salt2.length());
        assertEquals(32, salt3.length());
    }


    @Test
    public void isGeneratesADetermenisticPasswordHash() {
        final String hashedPassword = PasswordUtils.encryptPassword("username", "password", EXAMPLE_SALT);
        assertEquals("c445d8573b2433525436d20c0a14c80eb9aee1a6e72a6f42e17994ee92fd25c1db4d7d2b96b4e9e2d35adca4ee92a3e29eadc706f39ad05c9afb95267dd25c79", hashedPassword);
    }

}