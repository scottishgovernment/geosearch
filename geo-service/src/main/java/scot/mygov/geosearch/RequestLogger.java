package scot.mygov.geosearch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import java.io.IOException;

public class RequestLogger implements ContainerResponseFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(Geosearch.class);

    @Inject
    public RequestLogger() {
        // Default constructor
    }

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response)
            throws IOException {
        String method = request.getRequest().getMethod();
        String path = request.getUriInfo().getPath();
        int status = response.getStatus();
        LOGGER.info("{} {} {}",
                status,
                method,
                path);
    }

}
