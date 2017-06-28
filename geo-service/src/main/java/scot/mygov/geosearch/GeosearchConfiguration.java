package scot.mygov.geosearch;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.File;

public class GeosearchConfiguration {

    @Min(0)
    @Max(65535)
    private int port = 9092;

    private File codepoint;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public File getCodepoint() {
        return codepoint;
    }

    public void setCodepoint(File codepoint) {
        this.codepoint = codepoint;
    }

}
