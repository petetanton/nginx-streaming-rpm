package uk.tanton.streaming.live.dynamo.domain;

import org.junit.Test;

import java.time.Instant;
import java.util.Date;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class PublisherTest {

    @Test
    public void itDoesEqual() {
        final Publisher publisher1 = new Publisher(1, "somepasswordhash", "passwordsalt", "username", new Date(), new Date());
        final Publisher publisher2 = new Publisher(1, "somepasswordhash", "passwordsalt", "username", new Date(), new Date());

        assertTrue("Equals method is not working", publisher1.equals(publisher2));
        assertTrue(publisher1.hashCode() == publisher2.hashCode());
    }

    @Test
    public void itDoesNotEqual() {
        final Publisher publisher1 = new Publisher(1, "somepasswordhash", "passwordsalt", "username", new Date(Instant.now().minusSeconds(360).toEpochMilli()), new Date());
        final Publisher publisher2 = new Publisher(1, "somepasswordhash", "passwordsalt", "username", new Date(), new Date());

        assertFalse("Equals method is not working", publisher1.equals(publisher2));
        assertTrue(publisher1.hashCode() != publisher2.hashCode());
    }

    @Test
    public void itDoesNotEqual2() {
        final Publisher publisher1 = new Publisher(1, "somepasswordhash", "passwordssalt", "username", new Date(), new Date());
        final Publisher publisher2 = new Publisher(1, "somepasswordhash", "passwordsalt", "username", new Date(), new Date());

        assertFalse("Equals method is not working", publisher1.equals(publisher2));
        assertTrue(publisher1.hashCode() != publisher2.hashCode());
    }

}