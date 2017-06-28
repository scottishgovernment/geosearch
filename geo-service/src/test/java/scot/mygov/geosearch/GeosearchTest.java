package scot.mygov.geosearch;

import dagger.ObjectGraph;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GeosearchTest {

    @Test
    public void hasADefaultPort() {
        ObjectGraph graph = ObjectGraph.create(new GeosearchModule());
        Geosearch geosearch = graph.get(Geosearch.class);
        GeosearchConfiguration config = geosearch.config;
        assertEquals(9092, config.getPort());
    }

}
