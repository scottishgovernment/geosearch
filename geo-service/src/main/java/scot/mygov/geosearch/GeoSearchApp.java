package scot.mygov.geosearch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableConfigurationProperties(GeoSearchConfiguration.class)
public class GeoSearchApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeoSearchApp.class);

    GeoSearchApp() {
        // Default constructor.
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext context =
                new SpringApplicationBuilder(GeoSearchApp.class)
                        .application()
                        .run(args);
        showConfiguration(context);
    }

    private static void showConfiguration(ConfigurableApplicationContext context) {
        ConfigurableEnvironment env = context.getEnvironment();
        List<PropertySource<?>> sources = new ArrayList<>();
        env.getPropertySources().forEach(sources::add);
        String config = sources.stream()
                .filter(src -> src.getName().startsWith("applicationConfig"))
                .map(EnumerablePropertySource.class::cast)
                .flatMap(src -> asList(src.getPropertyNames()).stream())
                .map(key -> String.format("\t%s=%s", key, env.getProperty(key)))
                .collect(joining("\n"));
        LOGGER.info("\nEFFECTIVE PROPERTIES:\n{}", config);
    }

}
