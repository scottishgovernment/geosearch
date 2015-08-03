package scot.mygov.geosearch.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import scot.mygov.geosearch.api.models.Postcode;
import scot.mygov.geosearch.repositories.PostcodeRepository;

import javax.inject.Inject;

@RestController
public class PostcodesController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostcodesController.class);

    private PostcodeRepository postcodes;

    @Inject
    public PostcodesController(PostcodeRepository postcodes) {
        this.postcodes = postcodes;
    }

    @RequestMapping(
            value = "/postcodes/{postcode}",
            method = RequestMethod.GET
    )
    public Postcode get(@PathVariable("postcode") String postcode) {
        Postcode code = postcodes.getPostcode(postcode);
        if (code == null) {
            LOGGER.info("Unknown: {}", postcode);
            throw new NotFoundException(postcode);
        }
        LOGGER.info("Found: {}", postcode);
        return code;
    }

    @ControllerAdvice
    static class PostcodesExceptionHandler extends ResponseEntityExceptionHandler {
        @ExceptionHandler(NotFoundException.class)
        protected ResponseEntity<Object> handleInvalidRequest(RuntimeException ex, WebRequest request) {
            return handleExceptionInternal(ex, null, null, HttpStatus.NOT_FOUND, request);
        }
    }

}
