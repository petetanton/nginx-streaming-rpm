package uk.tanton.streaming.live;

import dagger.Component;
import uk.tanton.streaming.live.http.HttpHandler;
import uk.tanton.streaming.live.http.HttpServer;
import uk.tanton.streaming.live.modules.AwsModules;
import uk.tanton.streaming.live.modules.HttpModule;

import javax.inject.Inject;
import javax.inject.Singleton;

@Component(modules = {AwsModules.class, HttpModule.class})
@Singleton
interface MainExecComponent {
    Main main();
}

public class Main {
    private final HttpServer httpServer;

    @Inject
    public Main(final HttpServer httpServer) {
        this.httpServer = httpServer;
    }

    public static void main(String[] args) throws Exception {
        MainExecComponent mainComponent;
        try {
            mainComponent = DaggerMainExecComponent.builder()
                    .awsModules(new AwsModules())
                    .httpModule(new HttpModule())
                    .build();
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            System.exit(2);
            throw new AssertionError("unreachable");
        }

        Main main = mainComponent.main();
        main.start();
    }

    void start() throws Exception {
        httpServer.start(new HttpHandler());
    }

}
