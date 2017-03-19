package uk.tanton.streaming.live.modules;

import dagger.Module;
import dagger.Provides;
import uk.tanton.streaming.live.http.HttpServer;

import javax.inject.Singleton;

@Module
public class HttpModule {

    @Provides
    @Singleton
    HttpServer provideHttpServer() {
        return new HttpServer();
    }
}
