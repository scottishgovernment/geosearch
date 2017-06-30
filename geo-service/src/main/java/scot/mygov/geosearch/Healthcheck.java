package scot.mygov.geosearch;

import scot.mygov.geosearch.repositories.ZipPostcodeRepository;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.time.LocalDate;

@Path("health")
@Produces(MediaType.APPLICATION_JSON)
public class Healthcheck {

    private final ZipPostcodeRepository zipPostcodeRepository;

    @Inject
    public Healthcheck(ZipPostcodeRepository zipPostcodeRepository) {
        this.zipPostcodeRepository = zipPostcodeRepository;
    }

    @GET
    public Result get() {
        LocalDate copyrightDate = zipPostcodeRepository.getCopyrightDate();
        return new Result(copyrightDate.toString());
    }

    public static class Result {

        private final String copyrightDate;

        public Result(String copyrightDate) {
            this.copyrightDate = copyrightDate;
        }

        public String getCopyrightDate() {
            return copyrightDate;
        }
    }

}
