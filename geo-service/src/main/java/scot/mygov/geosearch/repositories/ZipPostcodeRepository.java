package scot.mygov.geosearch.repositories;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scot.mygov.geosearch.GeosearchConfiguration;
import scot.mygov.geosearch.api.models.Postcode;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class ZipPostcodeRepository implements PostcodeRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZipPostcodeRepository.class);

    private static final Charset CHARSET = Charset.forName("ISO-8859-1");

    private GeosearchConfiguration configuration;

    private static Map<String, Postcode> postcodes = new HashMap<>(200000);

    private static LocalDate copyrightDate;

    public Postcode getPostcode(String postcode) {
        String trimmed = trim(postcode);
        return postcodes.get(trimmed);
    }

    public LocalDate getCopyrightDate() {
        return copyrightDate;
    }

    @Inject
    public ZipPostcodeRepository(GeosearchConfiguration configuration) {
        this.configuration = configuration;
    }

    @PostConstruct
    public void load() throws IOException {
        long start = System.currentTimeMillis();
        File jar = findCodepointJar();
        loadFromJarFile(jar);
        long time = System.currentTimeMillis() - start;
        LOGGER.info("Loaded {} postcodes in {}ms", postcodes.size(), time);
    }

    File findCodepointJar() throws IOException {
        File jar = configuration.getCodepoint();
        if (isEmpty(jar)) {
            ClassLoader loader = getClass().getClassLoader();
            jar = classpathJar(loader);
        }
        return jar;
    }

    private boolean isEmpty(File file) {
        return file == null || file.toString().length() == 0;
    }

    File classpathJar(ClassLoader loader) throws IOException {
        String resource = "META-INF/maven/scot.mygov.os/os-codepoint/";
        URL url = loader.getResource(resource);
        if (url == null) {
            throw new IOException("Could not locate geosearch data");
        }
        String file = url.getFile();
        try {
            URL jarUrl = new URL(file.substring(0, file.indexOf('!')));
            return new File(jarUrl.toURI());
        } catch (URISyntaxException | MalformedURLException ex) {
            throw new IOException("Could not determine location of geosearch data.", ex);
        }
    }

    void loadFromJarFile(File jar) throws IOException {
        FileInputStream is = null;
        try {
            is = new FileInputStream(jar);
            loadFromJarStream(is);
        } catch (IOException ex) {
            closeQuietly(is);
            throw ex;
        }
    }

    void loadFromJarStream(InputStream is) throws IOException {
        try (JarInputStream jis = new JarInputStream(is)) {
            for (JarEntry entry = jis.getNextJarEntry(); entry != null; entry = jis.getNextJarEntry()) {
                if (isPostcodeCSV(entry)) {
                    readEntry(jis);
                }

                if (isMetadata(entry)) {
                    readMetadata(jis);
                }
            }
        }
    }

    private static boolean isPostcodeCSV(JarEntry entry) {
        return entry.getName().matches(".*/[a-z][a-z]?.csv");
    }

    private static boolean isMetadata(JarEntry entry) {
        return entry.getName().matches(".*Doc/metadata.txt");
    }

    private void readEntry(JarInputStream is) throws IOException {
        // Should call closeEntry() if an exception occurs, not close().
        // Hence, not using try-with-resources.
        try {
            InputStreamReader reader = new InputStreamReader(is, CHARSET);
            loadPostcodes(reader);
        } finally {
            is.closeEntry();
        }
    }

    private void readMetadata(JarInputStream is) throws IOException {
        // Should call closeEntry() if an exception occurs, not close().
        // Hence, not using try-with-resources.
        try {
            InputStreamReader reader = new InputStreamReader(is, CHARSET);
            loadMetadata(reader);
        } finally {
            is.closeEntry();
        }
    }

    private void loadPostcodes(Reader reader) throws IOException {
        CSVParser parse = new CSVParser(reader, CSVFormat.DEFAULT);
        for (CSVRecord record : parse) {
            String district = record.get(8);
            if (district.startsWith("S")) {
                Postcode postcode = new Postcode();
                postcode.setPostcode(record.get(0));
                postcode.setDistrict(district);
                postcode.setNormalisedPostcode(normalise(postcode.getPostcode()));
                postcodes.put(trim(postcode.getPostcode()), postcode);
            }
        }
    }

    private String normalise(String pc) {
        if (pc.charAt(3) == ' ') {
            return pc;
        } else {
            return String.format("%s %s", pc.substring(0, 4), pc.substring(4));
        }
    }

    private void loadMetadata(Reader reader) throws IOException {
        LineNumberReader lineNumberReader = new LineNumberReader(reader);
        String line;
        while ((line = lineNumberReader.readLine()) != null) {
            if (line.startsWith("COPYRIGHT DATE: ")) {
                String dateString = line.split(":")[1].trim();
                copyrightDate = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyyMMdd"));
            }
        }
    }

    /**
     * Removes any spaces in a postcode.
     */
    private static String trim(String str) {
        return str.replace(" ", "");
    }

    static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ex) {
            LOGGER.error("IOException occurred", ex);
        }
    }

}
