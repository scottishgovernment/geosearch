package scot.mygov.geosearch.repositories;

import org.junit.BeforeClass;
import org.junit.Test;
import scot.mygov.geosearch.GeosearchConfiguration;
import scot.mygov.geosearch.api.models.Postcode;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class ZipPostcodeRepositoryTest {

    private static GeosearchConfiguration config = new GeosearchConfiguration();

    private static ZipPostcodeRepository postcodes;

    @BeforeClass
    public static void setUpClass() throws IOException {
        postcodes = new ZipPostcodeRepository(config);
        postcodes.load();
    }

    @Test
    public void findCodePointDataOnClasspathIfConfigurationItemIsNullOrEmpty() throws IOException {
        File file = postcodes.findCodepointJar();
        assertNotNull(file);
        assertTrue(file.toString().matches(".*codepoint.*\\.jar"));
        config.setCodepoint(new File(""));
        file = postcodes.findCodepointJar();
        assertTrue(file.toString().matches(".*codepoint.*\\.jar"));
    }

    @Test
    public void findCodePointDataOnFileSystem() throws IOException {
        File file = new File("/tmp/codepoint.jar");
        config.setCodepoint(file);
        assertEquals(file, postcodes.findCodepointJar());
    }

    @Test(expected = IOException.class)
    public void throwsIfCodePointExpectedOnClasspathButNotFound() throws IOException {
        ClassLoader loader = new URLClassLoader(new URL[0]) {
            @Override
            public URL getResource(String name) {
                return null;
            }
        };
        postcodes.classpathJar(loader);
    }

    @Test
    public void loadsPostcodeDataFromJarFile() throws IOException, URISyntaxException {
        Postcode postcode = postcodes.getPostcode("DD1 1AD");
        assertEquals("DD1 1AD", postcode.getPostcode());
        assertEquals("S12000042", postcode.getDistrict());
        assertTrue(postcodes.getCopyrightDate().getYear() >= 2020);
    }

    @Test
    public void findsPostcodeFromPostcodeWithNoSpace() throws IOException {
        Postcode postcode = postcodes.getPostcode("DD108QR");
        assertEquals("DD10 8QR", postcode.getPostcode());
        assertEquals("S12000041", postcode.getDistrict());
    }

    @Test
    public void englishPostcodeIgnored() throws IOException, URISyntaxException {
        assertNull(postcodes.getPostcode("TD9 0TJ"));
    }

    @Test
    public void closeIgnoresNull() {
        ZipPostcodeRepository.closeQuietly(null);
    }

    @Test
    public void closesACloseable() {
        AtomicBoolean called = new AtomicBoolean(true);
        Closeable closeable = new Closeable() {
            @Override
            public void close() throws IOException {
                called.set(true);
            }
        };
        ZipPostcodeRepository.closeQuietly(closeable);
        assertTrue(called.get());
    }

}
