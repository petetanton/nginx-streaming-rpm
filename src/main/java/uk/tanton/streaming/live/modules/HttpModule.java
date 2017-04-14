package uk.tanton.streaming.live.modules;

import dagger.Module;
import dagger.Provides;
import uk.tanton.streaming.live.StreamAuthenticator;
import uk.tanton.streaming.live.dynamo.StreamDataConnector;
import uk.tanton.streaming.live.http.HttpServer;

import javax.inject.Named;
import javax.inject.Singleton;

@Module
public class HttpModule {

    @Provides
    @Singleton
    HttpServer provideHttpServer(@Named("streamDataConnector") final StreamDataConnector streamDataConnector) {
        return new HttpServer(new StreamAuthenticator(streamDataConnector));
    }
}
