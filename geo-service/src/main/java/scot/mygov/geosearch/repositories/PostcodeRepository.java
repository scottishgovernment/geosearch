package scot.mygov.geosearch.repositories;

import scot.mygov.geosearch.api.models.Postcode;

public interface PostcodeRepository {

    /**
     * Returns information about the given postcode.
     */
    Postcode getPostcode(String postcode);

}
