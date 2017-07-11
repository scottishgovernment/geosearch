package scot.mygov.geosearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Before;
import org.junit.Test;
import scot.mygov.geosearch.repositories.ZipPostcodeRepository;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HealthcheckTest {

    private ObjectMapper mapper;

    private Dispatcher dispatcher;

    private Healthcheck healthcheck;

    private MockHttpRequest request;

    private MockHttpResponse response;

    private ObjectNode health;

    @Before
    public void setUp() throws URISyntaxException {
        mapper = new ObjectMapper();
        healthcheck = new Healthcheck();
        healthcheck.clock = Clock.systemDefaultZone();
        healthcheck.zipPostcodeRepository = mock(ZipPostcodeRepository.class);
        dispatcher = MockDispatcherFactory.createDispatcher();
        dispatcher.getRegistry().addSingletonResource(healthcheck);
        request = MockHttpRequest.get("health");
        request.accept(MediaType.APPLICATION_JSON_TYPE);
        response = new MockHttpResponse();
    }

    @Test
    public void returnsTheCodepointCopyrightDate() {
        healthcheck.zipPostcodeRepository = repoWithDate(LocalDate.of(2017, 04, 15));
        health = get(200);
        assertEquals("2017-04-15", health.get("copyrightDate").asText());
    }

    @Test
    public void reportsHealthyIfCodepointDataIsRecent() {
        healthcheck.clock = date(LocalDate.of(2017, 6, 1));
        healthcheck.zipPostcodeRepository = repoWithDate(LocalDate.of(2017, 04, 15));
        health = get(200);
        assertEquals("OK", health.get("status").asText());
    }

    @Test
    public void reportWarningIfCodepointDataIsOutOfDate() {
        healthcheck.clock = date(LocalDate.of(2017, 9, 1));
        healthcheck.zipPostcodeRepository = repoWithDate(LocalDate.of(2017, 04, 15));
        health = get(200);
        assertEquals("WARNING", health.get("status").asText());
    }

    @Test
    public void reportHealthyIfCodepointDataIsOutOfDateButInGracePeriod() throws URISyntaxException {
        healthcheck.clock = date(LocalDate.of(2017, 9, 1));
        healthcheck.zipPostcodeRepository = repoWithDate(LocalDate.of(2017, 04, 15));
        request = MockHttpRequest.get("health?grace=P2M");
        health = get(200);
        assertEquals("OK", health.get("status").asText());
    }

    @Test
    public void returnsBadRequestIfGracePeriodIsNegative() throws URISyntaxException {
        request = MockHttpRequest.get("health?grace=P-1M");
        health = get(400);
    }

    Clock date(LocalDate date) {
        ZoneId zoneId = ZoneId.of("Europe/London");
        ZonedDateTime dateTime = date.atStartOfDay().atZone(zoneId);
        return Clock.fixed(dateTime.toInstant(), zoneId);
    }

    ZipPostcodeRepository repoWithDate(LocalDate date) {
        ZipPostcodeRepository repository = mock(ZipPostcodeRepository.class);
        when(repository.getCopyrightDate()).thenReturn(date);
        return repository;
    }

    ObjectNode get(int expectedStatus) {
        dispatcher.invoke(request, response);
        assertEquals(expectedStatus, response.getStatus());
        String contentAsString = response.getContentAsString();
        if (contentAsString == null || contentAsString.isEmpty()) {
            return null;
        }
        try {
            JsonNode node = mapper.readTree(contentAsString);
            assertEquals(node.getNodeType(), JsonNodeType.OBJECT);
            return (ObjectNode) node;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
