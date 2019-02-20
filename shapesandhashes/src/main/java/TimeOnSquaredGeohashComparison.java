import ch.hsr.geohash.GeoHash;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class TimeOnSquaredGeohashComparison {

    public static void main(String[] args) throws IOException {
        String folderName = "C:\\interchange\\shapefiler\\oresund3";
        List<GeoHash> norwayHashes = Files.lines(Paths.get(folderName, "hashes_5_compressed.csv"))
                .map(GeoHash::fromGeohashString)
                .collect(Collectors.toList());
        List<GeoHash> denmarkHashes = Files.lines(Paths.get(folderName,"shape_se_compressed.csv"))
        //List<GeoHash> denmarkHashes = Files.lines(Paths.get(folderName,"hashes_5_compressed_and_moved.csv"))
                .map(GeoHash::fromGeohashString)
                .collect(Collectors.toList());

        for (int i = 0; i < 1000; i++) {
            long start = System.currentTimeMillis();
            GeoHashArea norway = new GeoHashArea(norwayHashes);
            GeoHashArea denmark = new GeoHashArea(denmarkHashes);
            //norway.intersects(denmark);
            denmark.intersects(norway);
            long end = System.currentTimeMillis();
            System.out.println(String.format("%d: %d ms",i,end - start));
        }
    }
}
