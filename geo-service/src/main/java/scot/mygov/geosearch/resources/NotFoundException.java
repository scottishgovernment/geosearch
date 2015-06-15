package scot.mygov.geosearch.resources;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {

    public NotFoundException(String postcode) {
        super("Unknown postcode: " + postcode);
    }

}
