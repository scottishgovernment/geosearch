package scot.mygov.geosearch.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

public class ErrorHandler implements ExceptionMapper<Exception> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorHandler.class);

    @Inject
    public ErrorHandler() {
        // Default constructor
    }

    @Override
    public Response toResponse(Exception exception) {
        LOGGER.error("Exception thrown", exception);
        return Response.status(INTERNAL_SERVER_ERROR)
                .type(MediaType.TEXT_PLAIN_TYPE)
                .entity("error\n")
                .build();
    }

}
