package uk.tanton.streaming.live;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.tanton.streaming.live.dynamo.StreamDataConnector;
import uk.tanton.streaming.live.dynamo.domain.Publisher;
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
        final Publisher publisher = streamDataConnector.getPublisher(stream.getUser());

        if (publisher == null) {
            LOG.error(String.format("%s user does not exist in publisher table", stream.getUser()));
            return false;
        }

        final String passwordHashAttempt = PasswordUtils.encryptPassword(stream.getUser(), stream.getPassword(), publisher.getPasswordSalt());
        if (passwordHashAttempt.equals(publisher.getPasswordHash())) {
            LOG.info("%s authenticated", stream.getUser());
            return true;
        }

        LOG.error(String.format("%s inccorect password attempt", stream.getUser()));

        return false;
    }
}
