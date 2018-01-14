package uk.tanton.streaming.live;

import dagger.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.tanton.streaming.live.http.HttpServer;
import uk.tanton.streaming.live.modules.AwsModules;
import uk.tanton.streaming.live.modules.HttpModule;

import javax.inject.Inject;
import javax.inject.Singleton;

@Component(modules = {AwsModules.class, HttpModule.class})
@Singleton
@FunctionalInterface
interface MainExecComponent {
    Main main();
}

public class Main {
    private static final Logger LOG = LogManager.getLogger(Main.class);

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
            LOG.error(ex);
            System.exit(2);
            throw new AssertionError("unreachable");
        }

        Main main = mainComponent.main();
        main.start();
    }

    void start() throws InterruptedException {
        httpServer.start();
    }

}
