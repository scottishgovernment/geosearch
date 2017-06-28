package scot.mygov.geosearch;

import dagger.ObjectGraph;
import io.undertow.Undertow;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.net.InetSocketAddress;

public class Geosearch {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(Geosearch.class);

    @Inject
    GeosearchConfiguration config;

    @Inject
    GeosearchApplication app;

    public static final void main(String[] args) {
        ObjectGraph graph = ObjectGraph.create(new GeosearchModule());
        graph.get(Geosearch.class).run();
    }

    public void run() {
        Server server = new Server();
        server.deploy(app);
        server.start(Undertow.builder().addHttpListener(config.getPort(), "::"));
        LOGGER.info("Listening on port {}", server.port());
    }

    public static class Server extends UndertowJaxrsServer {
        public int port() {
            InetSocketAddress address = (InetSocketAddress) server
                    .getListenerInfo()
                    .get(0)
                    .getAddress();
            return address.getPort();
        }
    }

}
