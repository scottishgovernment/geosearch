package scot.mygov.geosearch.resources;

import org.junit.Before;
import org.junit.Test;
import scot.mygov.geosearch.api.models.Postcode;
import scot.mygov.geosearch.repositories.PostcodeRepository;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class PostcodeControllerTest {

    private PostcodesController controller;

    @Before
    public void setUp() {
        controller = new PostcodesController(new MapPostcodeRepository());
    }

    @Test
    public void returnsPostcodeWithLocalAuthority() {
        Postcode postcode = controller.get("DD1 1AA");
        assertEquals("DD1 1AA", postcode.getPostcode());
        assertEquals("S12000042", postcode.getDistrict());
    }

    @Test(expected = NotFoundException.class)
    public void throwsIfPostcodeNotFound() {
        controller.get("EH6 6QQ");
    }

    static class MapPostcodeRepository implements PostcodeRepository {

        Map<String, Postcode> postcodes = new HashMap<>();

        public MapPostcodeRepository() {
            Postcode p1 = new Postcode();
            p1.setPostcode("DD1 1AA");
            p1.setDistrict("S12000042");
            postcodes.put("DD11AA", p1);
        }

        @Override
        public Postcode getPostcode(String postcode) {
            return postcodes.get(postcode.replace(" ", ""));
        }

    }

}
