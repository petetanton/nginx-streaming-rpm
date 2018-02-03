package uk.tanton.streaming.live;

import dagger.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.tanton.streaming.live.http.HttpServer;
import uk.tanton.streaming.live.modules.AwsModules;
import uk.tanton.streaming.live.modules.HttpModule;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Component(modules = {AwsModules.class, HttpModule.class})
@Singleton
@FunctionalInterface
interface MainExecComponent {
    Main main();
}

public class Main {
    private static final Logger LOG = LogManager.getLogger(Main.class);

    private final HttpServer authServer;
    private final HttpServer clientServer;

    @Inject
    public Main(@Named("auth") HttpServer authServer, @Named("client") HttpServer clientServer) {
        this.authServer = authServer;
        this.clientServer = clientServer;
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
        authServer.start();
        clientServer.start();
    }

}
