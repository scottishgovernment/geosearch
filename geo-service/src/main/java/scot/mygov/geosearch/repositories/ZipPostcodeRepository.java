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
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class ZipPostcodeRepository implements PostcodeRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZipPostcodeRepository.class);

    private static final Charset CHARSET = Charset.forName("ISO-8859-1");

    private GeosearchConfiguration configuration;

    private static Map<String, Postcode> postcodes = new HashMap<>(200000);

    public Postcode getPostcode(String postcode) {
        String trimmed = trim(postcode);
        return postcodes.get(trimmed);
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
            }
        }
    }

    private static boolean isPostcodeCSV(JarEntry entry) {
        return entry.getName().matches(".*/[a-z][a-z]?.csv");
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

    private void loadPostcodes(Reader reader) throws IOException {
        CSVParser parse = new CSVParser(reader, CSVFormat.DEFAULT);
        for (CSVRecord record : parse) {
            Postcode postcode = new Postcode();
            postcode.setPostcode(record.get(0));
            postcode.setDistrict(record.get(8));
            if (postcode.getDistrict().startsWith("S")) {
                postcodes.put(trim(postcode.getPostcode()), postcode);
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
