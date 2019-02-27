import org.geotools.data.*;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.util.URLs;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ShapesFromLineSegments {

    public static void main(String[] args) throws IOException, FactoryException {
        List<LineString> lineStrings = Files.lines(Paths.get("C:\\interchange\\datex_eksempler", "linesegments_1550841372_unique.txt"))
                .map(ShapesFromLineSegments::createLineString)
                .collect(Collectors.toList());
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setCRS(CRS.decode("EPSG:4326"));
        builder.setName("linestrings");
        builder.add("the_geom",LineString.class);
        SimpleFeatureType schema = builder.buildFeatureType();

        String outFileName = "linestrings_getSituation_" + System.currentTimeMillis();
        File outFile = new File("C:\\interchange\\datex_eksempler\\" + outFileName + ".shp");
        Map<String, Serializable> creationParams = new HashMap<>();
        creationParams.put("url", URLs.fileToUrl(outFile));


        List<SimpleFeature> featureList = new ArrayList<>();
        for (LineString lineString :  lineStrings) {
            SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(schema);
            featureBuilder.add(lineString);
            featureList.add(featureBuilder.buildFeature(null));
        }
        SimpleFeatureCollection featureCollection = new ListFeatureCollection(schema,featureList);

        FileDataStoreFactorySpi factory = FileDataStoreFinder.getDataStoreFactory(".shp");
        DataStore outStore = factory.createDataStore(creationParams);
        outStore.createSchema(schema);

        SimpleFeatureStore featureStore = (SimpleFeatureStore)outStore.getFeatureSource(outFileName);
        Transaction t = new DefaultTransaction();
        try {
            featureStore.addFeatures(featureCollection);
            t.commit();
        } catch (IOException e) {
            try {
                t.rollback();

            } catch (IOException e2) {
            }
        } finally {
            t.close();
        }

    }

    public static LineString createLineString(String input) {
        GeometryFactory factory = JTSFactoryFinder.getGeometryFactory();
        String[] split = input.split(", ");
        Coordinate[] coordinates = new Coordinate[split.length];
        for (int i = 0; i < split.length; i++) {
            coordinates[i] = createCoordinate(split[i].split(" "));
        }
        return factory.createLineString(coordinates);
    }

    public static Coordinate createCoordinate(String[] coordString) {
        return new Coordinate(Double.parseDouble(coordString[0]),Double.parseDouble(coordString[1]));
    }
}
