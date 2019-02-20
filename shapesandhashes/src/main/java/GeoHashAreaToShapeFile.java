import ch.hsr.geohash.GeoHash;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GeoHashAreaToShapeFile {

    public static void main(String[] args) throws IOException {
        GeoHashArea area1 = GeoHashArea.getSampleGeohashArea(6);
        Collection<GeoHash> hashes = area1.getHashes();
        List<String> geoHashStrings = new ArrayList<>();
        for (GeoHash hash : hashes) {
            geoHashStrings.add(hash.toBase32());
        }
        SimpleFeatureType geohashScema = ShapeHasher.createGeohashScema();
        List<SimpleFeature> features = HashToShape.createFeaturesFromHashes(geoHashStrings,geohashScema);
        HashToShape.createShapeFromPolygons(geohashScema,features,"from_area","C:\\interchange\\shapefiler\\oresund3\\");



    }
}
