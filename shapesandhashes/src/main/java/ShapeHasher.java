import ch.hsr.geohash.GeoHash;
import org.geotools.data.*;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.util.URLs;
import org.locationtech.jts.geom.*;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.geometry.BoundingBox;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class ShapeHasher {


    public static void main(String[] args) throws IOException, CQLException {
        String pathname = "C:\\interchange\\shapefiler\\oresund3\\worldshape_10kmbuffer_oresund3.shp";
        String cqlPredicate = "ISO2 = 'NO'";
        SimpleFeatureCollection features = getFeaturesFromShape(pathname, cqlPredicate);


        //SimpleFeatureType schema = featureSource.getSchema();
        SimpleFeatureTypeBuilder schemabuilder = new SimpleFeatureTypeBuilder();
        schemabuilder.setName("Test");
        schemabuilder.add("the_geom",Polygon.class);
        schemabuilder.length(7).add("geohash",String.class);
        final SimpleFeatureType geoHashSchema = schemabuilder.buildFeatureType();

        System.out.println(geoHashSchema.toString());

        List<SimpleFeature> polygonList = new ArrayList<>();

        try (SimpleFeatureIterator iter = features.features()) {
            while (iter.hasNext()) {
                SimpleFeature feature = iter.next();
                System.out.println(feature.getType().getTypeName());
                Object defaultGeometry = feature.getDefaultGeometry();
                BoundingBox bounds = feature.getBounds();
                if (defaultGeometry instanceof MultiPolygon) {
                    MultiPolygon polygon = (MultiPolygon)defaultGeometry;
                    Point centroid = polygon.getCentroid();
                    Coordinate coordinate = centroid.getCoordinate();
                    for (int i = 1; i < 5;i++) {
                        String hash = GeoHash.geoHashStringWithCharacterPrecision(coordinate.y, coordinate.x, i);
                        Polygon polygonFromHash = createPolygonFromHash(hash);
                        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(geoHashSchema);
                        featureBuilder.add(polygonFromHash);
                        featureBuilder.add(hash);
                        polygonList.add(featureBuilder.buildFeature(null));
                        System.out.println(hash);
                    }
                } else {
                    System.out.println(String.format("Unknown geometry type: %s",defaultGeometry.getClass().getName()));
                }
                System.out.println(defaultGeometry.getClass().getName());

            }
        }

        File outFile = new File("C:\\interchange\\shapefiler\\oresund3\\kyrre_tester.shp");
        Map<String, Serializable> creationParams = new HashMap<>();
        creationParams.put("url", URLs.fileToUrl(outFile));
        FileDataStoreFactorySpi factory = FileDataStoreFinder.getDataStoreFactory("shp");
        DataStore outStore = factory.createDataStore(creationParams);
        outStore.createSchema(geoHashSchema); //Need to get the new schema somehow...

        SimpleFeatureStore featureStore = (SimpleFeatureStore) outStore.getFeatureSource("kyrre_tester");
        SimpleFeatureCollection collection = new ListFeatureCollection(geoHashSchema, polygonList);

        Transaction t = new DefaultTransaction();
        try {
            featureStore.addFeatures(collection);
            t.commit();
        } catch (IOException e) {
            try {
                t.rollback();
            } catch (IOException ee) {}
        } finally {
            t.close();
        }
    }

    public static SimpleFeatureCollection getFeaturesFromShape(String pathname, String cqlPredicate) throws IOException, CQLException {
        FileDataStore dataStore = FileDataStoreFinder.getDataStore(new File(pathname));
        SimpleFeatureSource featureSource = dataStore.getFeatureSource();
        Filter filter = CQL.toFilter(cqlPredicate);
        return featureSource.getFeatures(filter);
    }


    public static Polygon createPolygonFromHash(String hash) {
        GeometryFactory factory = JTSFactoryFinder.getGeometryFactory();
        GeoHash geoHash = GeoHash.fromGeohashString(hash);

        ch.hsr.geohash.BoundingBox boundingBox = geoHash.getBoundingBox();
        Coordinate[] coordinates = new Coordinate[] {
                new Coordinate(boundingBox.getMinLon(),boundingBox.getMinLat()),
                new Coordinate(boundingBox.getMaxLon(),boundingBox.getMinLat()),
                new Coordinate(boundingBox.getMaxLon(),boundingBox.getMaxLat()),
                new Coordinate(boundingBox.getMinLon(),boundingBox.getMaxLat()),
                new Coordinate(boundingBox.getMinLon(),boundingBox.getMinLat())};
        return factory.createPolygon(coordinates);
    }

    public static SimpleFeatureType createGeohashScema() {
        SimpleFeatureTypeBuilder schemabuilder = new SimpleFeatureTypeBuilder();
        schemabuilder.setName("Test");
        schemabuilder.add("the_geom", Polygon.class);
        schemabuilder.length(7).add("geohash",String.class);
        return schemabuilder.buildFeatureType();
    }
}
