import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PrefixChecker {

    public static void main(String[] args) throws IOException {

        Files.lines(Paths.get("C:\\interchange\\shapefiler\\oresund3\\hashes_5_compressed.csv"))
                .sorted()
                .distinct()
                .forEach(System.out::println);
    }
}
