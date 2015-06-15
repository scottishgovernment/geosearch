package scot.mygov.geosearch;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.File;

@ConfigurationProperties
public class GeoSearchConfiguration {

    private File codepoint;

    public File getCodepoint() {
        return codepoint;
    }

    public void setCodepoint(File codepoint) {
        this.codepoint = codepoint;
    }

}
