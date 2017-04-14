package uk.tanton.streaming.live;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.tanton.streaming.live.dynamo.StreamDataConnector;
import uk.tanton.streaming.live.dynamo.domain.Publisher;
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
    public void setup() throws NoSuchAlgorithmException {
        underTest = new StreamAuthenticator(streamDataConnector);


        final String salt = PasswordUtils.getSalt();
        final String passwordHash = PasswordUtils.encryptPassword("username", "some-password!", salt);
        when(streamDataConnector.getPublisher("username")).thenReturn(new Publisher("accountId", passwordHash, salt, "username", new Date(Instant.now().minusSeconds(60).toEpochMilli()), new Date(Instant.now().plusSeconds(60).toEpochMilli())));
    }

    @Test
    public void itAuthorisesCorrectPassword() {
        final boolean authorised = underTest.isAuthorised(new Stream("app", "name", "username", "some-password!"));

        assertTrue(authorised);
        verify(streamDataConnector).getPublisher("username");
        finish();
    }

    @Test
    public void itDoesNotAuthoriseIncorrectPasswords() {
        final boolean authorised = underTest.isAuthorised(new Stream("app", "name", "username", "balh-password"));

        assertFalse(authorised);
        verify(streamDataConnector).getPublisher("username");
        finish();
    }

    @Test
    public void itReturnsFalseIfPublisherDoesNotExist() {
        final boolean authorised = underTest.isAuthorised(new Stream("app", "name", "some-user-name", "some-password!"));

        assertFalse(authorised);
        verify(streamDataConnector).getPublisher("some-user-name");
        finish();
    }

    private void finish() {
        verifyNoMoreInteractions(streamDataConnector);
    }

}