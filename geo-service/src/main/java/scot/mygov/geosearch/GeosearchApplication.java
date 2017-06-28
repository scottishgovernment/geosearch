package scot.mygov.geosearch;

import scot.mygov.geosearch.resources.PostcodesController;

import javax.inject.Inject;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

public class GeosearchApplication extends Application {

    @Inject
    PostcodesController postcodes;

    @Inject
    RequestLogger logger;

    @Override
    public Set<Object> getSingletons() {
        return new HashSet<>(asList(
                postcodes,
                logger
        ));
    }


}
