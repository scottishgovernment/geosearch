package scot.mygov.geosearch.repositories;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scot.mygov.geosearch.GeosearchConfiguration;
import scot.mygov.geosearch.api.models.Authority;
import scot.mygov.geosearch.api.models.Postcode;
import scot.mygov.geosearch.api.models.Ward;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.Closeable;
import java.io.File;
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
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;

public class ZipPostcodeRepository implements PostcodeRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZipPostcodeRepository.class);

    private static final Charset CHARSET = Charset.forName("ISO-8859-1");

    private static final String DATA_PREFIX = "codepoint/";

    private static final Pattern CSV_FILE =
            Pattern.compile(DATA_PREFIX + "Data/CSV/[a-z]+\\.csv");

    private GeosearchConfiguration configuration;

    private static Map<String, Postcode> postcodes = new HashMap<>(200000);

    private static Map<String, Authority> authorities = new HashMap<>();

    private static Map<String, Ward> wards = new HashMap<>();

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
        JarFile file = new JarFile(jar);
        JarEntry metadataEntry = jarEntry(file, "Doc/metadata.txt");
        InputStream is = file.getInputStream(metadataEntry);
        InputStreamReader reader = new InputStreamReader(is, CHARSET);
        loadMetadata(reader);
        LOGGER.info("Data Copyright {}", copyrightDate);
        JarEntry codelistEntry = jarEntry(file, "Doc/Codelist.xlsx");
        InputStream is2 = file.getInputStream(codelistEntry);
        if (is2 != null) {
            readCodelist(is2);
            LOGGER.info("Loaded {} local authorities and {} wards",
                    authorities.size(),
                    wards.size());
        }
        List<JarEntry> entries = file.stream()
                .filter(entry -> CSV_FILE.matcher(entry.getName()).matches())
                .collect(toList());
        for (JarEntry entry : entries) {
            loadFromJarStream(file.getInputStream(entry));
        }
    }

    private JarEntry jarEntry(JarFile file, String name) {
        return file.getJarEntry(DATA_PREFIX + name);
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

    private void readCodelist(InputStream is) throws IOException {
        Workbook workbook = WorkbookFactory.create(is);
        loadAuthorities(workbook);
        loadWards(workbook);
    }

    private void loadAuthorities(Workbook workbook) {
        Sheet utaSheet = workbook.getSheet("UTA");
        Map<String, String> authorityMap = readSheet(utaSheet);
        for (Map.Entry<String, String> entry : authorityMap.entrySet()) {
            Authority authority = new Authority();
            authority.setCode(entry.getKey());
            authority.setName(entry.getValue());
            authorities.put(entry.getKey(), authority);
        }
    }

    private void loadWards(Workbook workbook) {
        Sheet utaSheet = workbook.getSheet("UTW");
        Map<String, String> wardMap = readSheet(utaSheet);
        for (Map.Entry<String, String> entry : wardMap.entrySet()) {
            Ward ward = new Ward();
            ward.setCode(entry.getKey());
            ward.setName(entry.getValue());
            wards.put(entry.getKey(), ward);
        }
    }

    private Map<String, String> readSheet(Sheet sheet) {
        int size = sheet.getLastRowNum() - sheet.getFirstRowNum();
        Map<String, String> names = new HashMap<>((size * 4) / 3);
        for (int i = sheet.getFirstRowNum(); i < sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            String id = row.getCell(1).getStringCellValue();
            String name = row.getCell(0).getStringCellValue();
            if (id.startsWith("S")) {
                names.put(id, name);
            }
        }
        return names;
    }

    public void loadFromJarStream(InputStream is) throws IOException {
        InputStreamReader reader = new InputStreamReader(is, CHARSET);
        loadPostcodes(reader);
    }

    private void loadPostcodes(Reader reader) throws IOException {
        CSVParser parse = new CSVParser(reader, CSVFormat.DEFAULT);
        for (CSVRecord record : parse) {
            String district = record.get(8);
            String ward = record.get(9);
            if (district.startsWith("S")) {
                String code = record.get(0);
                String normalised = normalise(code);
                Postcode postcode = new Postcode();
                postcode.setPostcode(normalised);
                postcode.setDistrict(district);

                postcodes.put(trim(postcode.getPostcode()), postcode);
            }
        }
    }

    private String normalise(String pc) {
        if (pc.charAt(3) == ' ') {
            return pc;
        }
        return String.format("%s %s", pc.substring(0, 4), pc.substring(4));
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
