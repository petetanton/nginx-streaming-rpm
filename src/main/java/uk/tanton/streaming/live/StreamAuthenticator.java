package uk.tanton.streaming.live;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.tanton.streaming.live.dynamo.StreamDataConnector;
import uk.tanton.streaming.live.dynamo.domain.Publisher;
import uk.tanton.streaming.live.exception.NoSuchPublisherException;
import uk.tanton.streaming.live.security.PasswordUtils;
import uk.tanton.streaming.live.streams.Stream;

import javax.inject.Inject;

public class StreamAuthenticator {
    private static final Logger LOG = LogManager.getLogger(StreamAuthenticator.class);

    private final StreamDataConnector streamDataConnector;

    @Inject
    public StreamAuthenticator(final StreamDataConnector streamDataConnector) {
        this.streamDataConnector = streamDataConnector;
    }

    public boolean isAuthorised(final Stream stream) {
        final Publisher publisher;
        try {
            publisher = streamDataConnector.getPublisher(stream.getUser());
        } catch (NoSuchPublisherException e) {
            return false;
        }

        final String passwordHashAttempt = PasswordUtils.encryptPassword(stream.getUser(), stream.getPassword(), publisher.getPasswordSalt());
        if (passwordHashAttempt.equals(publisher.getPasswordHash())) {
            LOG.info(String.format("%s authenticated", stream.getUser()));
            return true;
        }

        LOG.error(String.format("%s incorrect password attempt", stream.getUser()));

        return false;
    }

    public int getAccountForStream(Stream stream) throws NoSuchPublisherException {
        return streamDataConnector.getPublisher(stream.getUser()).getAccountId();
    }
}
