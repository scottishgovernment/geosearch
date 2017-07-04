package scot.mygov.geosearch;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("health")
public class Healthcheck {

    @Inject
    public Healthcheck() {
        // Default constructor
    }

    @GET
    public Response health() {
        return Response.status(200).build();
    }

}
