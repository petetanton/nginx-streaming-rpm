package uk.tanton.streaming.live;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.tanton.streaming.live.dynamo.StreamDataConnector;
import uk.tanton.streaming.live.dynamo.domain.Publisher;
import uk.tanton.streaming.live.exception.NoSuchPublisherException;
import uk.tanton.streaming.live.security.PasswordUtils;
import uk.tanton.streaming.live.streams.Stream;

import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StreamAuthenticatorTest {

    private StreamAuthenticator underTest;


    @Mock private StreamDataConnector streamDataConnector;

    @Before
    public void setup() throws NoSuchAlgorithmException, NoSuchPublisherException {
        underTest = new StreamAuthenticator(streamDataConnector);


        final String salt = PasswordUtils.getSalt();
        final String passwordHash = PasswordUtils.encryptPassword("username", "some-password!", salt);
        when(streamDataConnector.getPublisher("username")).thenReturn(new Publisher(1, passwordHash, salt, "username", new Date(Instant.now().minusSeconds(60).toEpochMilli()), new Date(Instant.now().plusSeconds(60).toEpochMilli())));
        when(streamDataConnector.getPublisher("some-user-name")).thenThrow(new NoSuchPublisherException("some-user-name"));
    }

    @Test
    public void itAuthorisesCorrectPassword() throws NoSuchPublisherException {
        final boolean authorised = underTest.isAuthorised(new Stream("app", "name", "username", "some-password!"));

        assertTrue(authorised);
        verify(streamDataConnector).getPublisher("username");
        finish();
    }

    @Test
    public void itDoesNotAuthoriseIncorrectPasswords() throws NoSuchPublisherException {
        final boolean authorised = underTest.isAuthorised(new Stream("app", "name", "username", "balh-password"));

        assertFalse(authorised);
        verify(streamDataConnector).getPublisher("username");
        finish();
    }

    @Test
    public void itReturnsFalseIfPublisherDoesNotExist() throws NoSuchPublisherException {
        final boolean authorised = underTest.isAuthorised(new Stream("app", "name", "some-user-name", "some-password!"));

        assertFalse(authorised);
        verify(streamDataConnector).getPublisher("some-user-name");
        finish();
    }

    private void finish() {
        verifyNoMoreInteractions(streamDataConnector);
    }

    @Test
    public void isCreatesAUser() throws NoSuchAlgorithmException {
        final String salt = PasswordUtils.getSalt();
        final String encryptPassword = PasswordUtils.encryptPassword("user", "password", salt);


        System.out.println(String.format("Salt: %s", salt));
        System.out.println(String.format("Encrypted password: %s", encryptPassword));
    }

}