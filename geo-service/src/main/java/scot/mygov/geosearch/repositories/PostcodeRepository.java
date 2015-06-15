package scot.mygov.geosearch.repositories;

import org.springframework.context.annotation.ComponentScan;
import scot.mygov.geosearch.api.models.Postcode;

@ComponentScan
public interface PostcodeRepository {

    /**
     * Returns information about the given postcode.
     */
    Postcode getPostcode(String postcode);

}
