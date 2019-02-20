import ch.hsr.geohash.GeoHash;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.*;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.BoundingBox;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MonteCarloShapeHasher {


    public static void main(String[] args) throws IOException, CQLException {
        String pathname = "C:\\interchange\\shapefiler\\oresund3\\worldshape_10kmbuffer_oresund3.shp";
        String cqlPredicate = "ISO2 = 'NO'";
        int depth = 5;
        int rounds = 400000;
        SimpleFeatureCollection features = ShapeHasher.getFeaturesFromShape(pathname, cqlPredicate);

        double minX = 0.0d;
        double minY = 0.0d;
        double width = 0.0d;
        double heigth = 0.0d;
        MultiPolygon poly = null;

        try (SimpleFeatureIterator iterator = features.features()) {
            while (iterator.hasNext()) {
                SimpleFeature feature = iterator.next();
                BoundingBox bounds = feature.getBounds();
                minX = bounds.getMinX();
                minY = bounds.getMinY();
                width = bounds.getWidth();
                heigth = bounds.getHeight();
                poly = (MultiPolygon) feature.getDefaultGeometry();
                System.out.println(String.format("minx: %f, miny: %f, width: %f, height: %f", minX, minY, width, heigth));
            }
        }

        GeometryFactory factory = JTSFactoryFinder.getGeometryFactory();
        Random random = new Random(1l);
        int numPoints = 0;
        List<String> hashes = new ArrayList<>();
        List<SimpleFeature> hashedFeatures = new ArrayList<>();
        SimpleFeatureType geohashScema = ShapeHasher.createGeohashScema();
        for (int i = 0; i < rounds; i++) {
            double randx = minX + (random.nextDouble() * width);
            double randy = minY + (random.nextDouble() * heigth);
            Coordinate randC = new Coordinate(randx, randy);
            Point point = factory.createPoint(randC);
            if (poly.contains(point)) {
                String hash = GeoHash.geoHashStringWithCharacterPrecision(randy, randx, depth);
                hashes.add(hash);
                Polygon polygonFromHash = ShapeHasher.createPolygonFromHash(hash);
                SimpleFeatureBuilder builder = new SimpleFeatureBuilder(geohashScema);
                builder.add(polygonFromHash);
                builder.add(hash);
                hashedFeatures.add(builder.buildFeature(null));
                numPoints++;
            }

        }
        System.out.println(String.format("Made %d points within the shape", numPoints));
        HashToShape.createShapeFromPolygons(geohashScema, hashedFeatures, "hashed_depth_" + depth + "_rounds_" + rounds, "C:\\interchange\\shapefiler\\oresund3\\");
        Files.write(Paths.get("C:\\interchange\\shapefiler\\oresund3\\hashed_depth" + depth + "_rounds_" + rounds + ".csv"),hashes, Charset.forName("UTF-8"));
    }
}
