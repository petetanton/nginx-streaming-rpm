package uk.tanton.streaming.live.modules;

import com.amazonaws.services.sqs.AmazonSQS;
import dagger.Module;
import dagger.Provides;
import org.apache.http.impl.client.HttpClientBuilder;
import uk.tanton.streaming.live.StreamAuthenticator;
import uk.tanton.streaming.live.StreamManager;
import uk.tanton.streaming.live.dynamo.StreamDataConnector;
import uk.tanton.streaming.live.http.HttpServer;

import javax.inject.Named;
import javax.inject.Singleton;

@Module
public class HttpModule {

    @Provides
    @Singleton
    HttpServer provideHttpServer(@Named("streamDataConnector") final StreamDataConnector streamDataConnector, @Named("sqs") final AmazonSQS sqs) {
        final StreamAuthenticator streamAuthenticator = new StreamAuthenticator(streamDataConnector);
        return new HttpServer(streamAuthenticator, new StreamManager(HttpClientBuilder.create().build(), sqs, streamAuthenticator));
    }
}
