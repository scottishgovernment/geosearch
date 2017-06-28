package scot.mygov.geosearch.resources;

import scot.mygov.geosearch.api.models.Postcode;
import scot.mygov.geosearch.repositories.PostcodeRepository;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Path("/postcodes")
@Produces("application/json")
public class PostcodesController {

    private PostcodeRepository postcodes;

    @Inject
    public PostcodesController(PostcodeRepository postcodes) {
        this.postcodes = postcodes;
    }

    @GET
    @Path("{postcode}")
    public Postcode get(@PathParam("postcode") String postcode) {
        Postcode code = postcodes.getPostcode(postcode);
        if (code == null) {
            throw new NotFoundException(postcode);
        }
        return code;
    }

}
