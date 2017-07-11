package scot.mygov.geosearch;

import dagger.Module;
import dagger.Provides;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scot.mygov.config.Configuration;
import scot.mygov.geosearch.repositories.PostcodeRepository;
import scot.mygov.geosearch.repositories.ZipPostcodeRepository;

import javax.inject.Singleton;
import java.io.IOException;
import java.time.Clock;

@Module(injects = Geosearch.class)
public class GeosearchModule {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(GeosearchConfiguration.class);

    private static final String APP_NAME = "geosearch";

    @Provides
    @Singleton
    GeosearchConfiguration configuration() {
        Configuration<GeosearchConfiguration> configuration = Configuration
                .load(new GeosearchConfiguration(), APP_NAME)
                .validate();
        LOGGER.info("{}", configuration);
        return configuration.getConfiguration();
    }

    @Provides
    @Singleton
    PostcodeRepository postcodeRepository(GeosearchConfiguration config) {
        ZipPostcodeRepository repository = new ZipPostcodeRepository(config);
        try {
            repository.load();
        } catch (IOException ex) {
            throw new RuntimeException("Could not load postcode data", ex);
        }
        return repository;
    }

    @Provides
    @Singleton
    Clock clock() {
        return Clock.systemDefaultZone();
    }

}
