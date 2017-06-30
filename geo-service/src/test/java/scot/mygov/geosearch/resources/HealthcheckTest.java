package scot.mygov.geosearch.resources;

import org.junit.Test;
import scot.mygov.geosearch.Healthcheck;
import scot.mygov.geosearch.repositories.ZipPostcodeRepository;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HealthcheckTest {

    @Test
    public void outOfDateDataIsUnhealthly() {

        // ARRANGE
        ZipPostcodeRepository repo = repoWithDate(LocalDate.MIN);
        Healthcheck sut = new Healthcheck(repo);

        // ACT
        Healthcheck.Result actual = sut.get();

        // ASSERT
        assertEquals("Has correct date", actual.getCopyrightDate(), LocalDate.MIN.toString());
    }

    ZipPostcodeRepository repoWithDate(LocalDate date) {
        ZipPostcodeRepository repository = mock(ZipPostcodeRepository.class);
        when(repository.getCopyrightDate()).thenReturn(date);
        return repository;
    }
}
