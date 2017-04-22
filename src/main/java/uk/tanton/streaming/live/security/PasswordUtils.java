package uk.tanton.streaming.live.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class PasswordUtils {
    private static final Logger LOG = LogManager.getLogger(PasswordUtils.class);

    public static String encryptPassword(final String user, final String password, final String salt) {

        String generatedPassword = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(fromBase64(salt));
            byte[] bytes = md.digest(String.format("%s:%s", user, password).getBytes());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            generatedPassword = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            LOG.error(e);
        }
        return generatedPassword;
    }

    public static String getSalt() throws NoSuchAlgorithmException {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[24];
        sr.nextBytes(salt);
        return toBase64(salt);
    }

    private static byte[] fromBase64(String hex) throws IllegalArgumentException {
        return DatatypeConverter.parseBase64Binary(hex);
    }

    private static String toBase64(byte[] array) {
        return DatatypeConverter.printBase64Binary(array);
    }
}
