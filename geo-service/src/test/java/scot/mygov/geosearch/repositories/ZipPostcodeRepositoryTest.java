package scot.mygov.geosearch.repositories;

import org.junit.Before;
import org.junit.Test;
import scot.mygov.geosearch.GeosearchConfiguration;
import scot.mygov.geosearch.api.models.Postcode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ZipPostcodeRepositoryTest {

    private GeosearchConfiguration config = new GeosearchConfiguration();

    private ZipPostcodeRepository postcodes;

    @Before
    public void setUp() throws IOException {
        postcodes = new ZipPostcodeRepository(config);
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
        byte[] bytes = jarWithTestData();
        URL url = getClass().getProtectionDomain().getCodeSource().getLocation();
        File testClasses = new File(url.toURI());
        File file = new File(testClasses, "test.jar");
        Files.write(file.toPath(), bytes, StandardOpenOption.CREATE);
        postcodes.loadFromJarFile(file);
        Postcode postcode = postcodes.getPostcode("DD1 1AD");
        assertEquals("DD1 1AD", postcode.getPostcode());
        assertEquals("S12000042", postcode.getDistrict());
        assertEquals(LocalDate.of(2015, 04, 28), postcodes.getCopyrightDate());
    }

    @Test
    public void loadsPostcodeDataFromJarStream() throws IOException {
        byte[] bytes = jarWithTestData();
        postcodes.loadFromJarStream(new ByteArrayInputStream(bytes));
        Postcode postcode = postcodes.getPostcode("DD1 1AD");
        assertEquals("DD1 1AD", postcode.getPostcode());
        assertEquals("S12000042", postcode.getDistrict());
    }

    @Test(expected = Exception.class)
    public void coverIOExceptionWhenReadingFromJarStream() throws IOException {
        postcodes.loadFromJarStream(null);
    }

    @Test
    public void englishPostcodeIgnored() throws IOException, URISyntaxException {
        byte[] bytes = jarWithTestData();
        postcodes.loadFromJarStream(new ByteArrayInputStream(bytes));
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

    private byte[] jarWithTestData() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JarOutputStream jos = new JarOutputStream(baos);
        // Add an entry that should be ignored.
        jos.putNextEntry(new ZipEntry("META-INF/maven/scot.mygov/geo-search/pom.properties"));
        jos.closeEntry();

        jos.putNextEntry(new ZipEntry("codepoint/Data/CSV/dd.csv"));
        copy(ZipPostcodeRepositoryTest.class.getResourceAsStream("dd.csv"), jos);
        jos.closeEntry();

        jos.putNextEntry(new ZipEntry("codepoint/Doc/metadata.txt"));

        copy(ZipPostcodeRepositoryTest.class.getResourceAsStream("metadata.txt"), jos);
        jos.closeEntry();


        jos.close();
        return baos.toByteArray();
    }

    void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read = in.read(buffer);
        while (read >= 0) {
            out.write(buffer, 0, read);
            read = in.read(buffer);
        }
    }

}
