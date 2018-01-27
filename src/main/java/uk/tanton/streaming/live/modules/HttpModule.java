package uk.tanton.streaming.live.modules;

import com.amazonaws.services.sqs.AmazonSQS;
import dagger.Module;
import dagger.Provides;
import org.apache.http.impl.client.HttpClientBuilder;
import uk.tanton.streaming.live.StreamAuthenticator;
import uk.tanton.streaming.live.dynamo.StreamDataConnector;
import uk.tanton.streaming.live.http.HttpServer;
import uk.tanton.streaming.live.http.ProxyClient;
import uk.tanton.streaming.live.managment.StreamManager;

import javax.inject.Named;
import javax.inject.Singleton;

@Module
public class HttpModule {
    private static final boolean SHOULD_SEND_SNS = false;

    @Provides
    @Singleton
    HttpServer provideHttpServer(@Named("streamDataConnector") final StreamDataConnector streamDataConnector, @Named("sqs") final AmazonSQS sqs) {
        final StreamAuthenticator streamAuthenticator = new StreamAuthenticator(streamDataConnector);
        final HttpClientBuilder httpClientBuilder = HttpClientBuilder.create()
                .setMaxConnTotal(100);
        return new HttpServer(streamAuthenticator, new StreamManager(httpClientBuilder.build(), sqs, streamAuthenticator, SHOULD_SEND_SNS, streamDataConnector), new ProxyClient(httpClientBuilder.build()));
    }
}
