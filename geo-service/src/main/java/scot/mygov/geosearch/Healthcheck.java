package scot.mygov.geosearch;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import scot.mygov.geosearch.repositories.ZipPostcodeRepository;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static java.lang.String.format;

@Path("health")
@Produces(MediaType.APPLICATION_JSON)
public class Healthcheck {

    private static final Period TIME_BETWEEN_CODEPOINT_RELEASES = Period.ofMonths(3);

    private static final JsonNodeFactory FACTORY = JsonNodeFactory.instance;

    @Inject
    Clock clock;

    @Inject
    ZipPostcodeRepository zipPostcodeRepository;

    @GET
    public ObjectNode get(
            @QueryParam("grace") @DefaultValue("P1M") String gracePeriod
    ) {
        Period grace = Period.parse(gracePeriod);
        if (grace.isNegative()) {
            throw new BadRequestException("Grace period cannot be negative");
        }
        LocalDate copyrightDate = zipPostcodeRepository.getCopyrightDate();
        LocalDate releaseDate = releaseDate(copyrightDate);
        Status status = isCurrent(clock, releaseDate, grace) ? Status.OK : Status.WARNING;
        String releaseString = releaseDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        ObjectNode result = FACTORY.objectNode();
        result.put("copyrightDate", copyrightDate.toString());
        result.put("status", status.name());
        result.put("message", format("Codepoint release: %s", releaseString));

        return result;
    }

    private LocalDate releaseDate(LocalDate copyrightDate) {
        // The Codepoint copyright date is somewhere in the month before the Codepoint release.
        // For example, the Codepoint release in May will have a copyright date in April.
        // Therefore, the release date is determined as the start of the following month.
        return copyrightDate
                .withDayOfMonth(1)
                .plusMonths(1);
    }

    public boolean isCurrent(Clock clock, LocalDate releaseDate, Period grace) {
        LocalDate deadline = releaseDate
                .plus(TIME_BETWEEN_CODEPOINT_RELEASES)
                .plus(grace);
        LocalDate now = clock.instant()
                .atZone(ZoneId.of("Europe/London"))
                .toLocalDate();
        return now.isBefore(deadline);
    }

    enum Status {
        OK,
        WARNING
    }

}
