package no.vegvesen.ixn.federation.matcher;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CsvReaderTestUtil {
    public static Object[] readParametersFromCSV(String csvFilename) throws IOException, CsvException {
        InputStream resourceAsStream = CsvReaderTestUtil.class.getClassLoader().getResourceAsStream(csvFilename);
        if (resourceAsStream != null) {
            InputStreamReader streamReader = new InputStreamReader(resourceAsStream);
            Object[] testParameters;
            try (CSVReader csvReader = new CSVReaderBuilder(streamReader).build()) {
                testParameters = csvReader.readAll().toArray();
            }
            return testParameters;
        } else {
            throw new RuntimeException("Csv File not exists");
        }
    }
}