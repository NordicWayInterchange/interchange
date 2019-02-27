import ch.hsr.geohash.GeoHash;
import ch.hsr.geohash.WGS84Point;
import ch.hsr.geohash.queries.GeoHashBoundingBoxQuery;
import ch.hsr.geohash.util.GeoHashSizeTable;
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
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.util.URLs;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.geometry.BoundingBox;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShapeHasher {


    public static void main(String[] args) throws IOException {
        String pathname = "C:\\interchange\\datex_eksempler\\linestrings_getSituation_1550841672078.shp";
        //String pathname = "C:\\interchange\\shapefiler\\oresund3\\E18_linjestykker_wgs84.shp";
        String outFileName = "getSituation";
        final String outFileExtension = "shp";
        File outFile = new File("C:\\interchange\\shapefiler\\oresund3\\" + outFileName + "." + outFileExtension);

        SimpleFeatureCollection features = getFeaturesFromShape(pathname);


        //SimpleFeatureType schema = featureSource.getSchema();
        SimpleFeatureTypeBuilder schemabuilder = new SimpleFeatureTypeBuilder();
        schemabuilder.setCRS(DefaultGeographicCRS.WGS84);
        schemabuilder.setName("Test");
        schemabuilder.add("the_geom",Polygon.class);
        schemabuilder.length(7).add("geohash",String.class);
        final SimpleFeatureType geoHashSchema = schemabuilder.buildFeatureType();

        System.out.println(geoHashSchema.toString());

        List<SimpleFeature> polygonList = new ArrayList<>();

        try (SimpleFeatureIterator iter = features.features()) {
            while (iter.hasNext()) {
                SimpleFeature feature = iter.next();
                ch.hsr.geohash.BoundingBox boundingBox = boundingBox2GeoHashBoundingBox(feature.getBounds());
                GeoHashBoundingBoxQuery geoHashBoundingBoxQuery = new GeoHashBoundingBoxQuery(boundingBox);
                System.out.println(geoHashBoundingBoxQuery);
                List<GeoHash> searchHashes = geoHashBoundingBoxQuery.getSearchHashes();

                for (GeoHash hash : searchHashes) {
                    System.out.println(hash.getCharacterPrecision());
                    Polygon polygonFromHash = createPolygonFromHash(hash);
                    SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(geoHashSchema);
                    featureBuilder.add(polygonFromHash);
                    featureBuilder.add(hash.toBase32());
                    polygonList.add(featureBuilder.buildFeature(null));
                    System.out.println(hash);
                }
            }
        }


        Map<String, Serializable> creationParams = new HashMap<>();
        creationParams.put("url", URLs.fileToUrl(outFile));
        FileDataStoreFactorySpi factory = FileDataStoreFinder.getDataStoreFactory(outFileExtension);
        DataStore outStore = factory.createDataStore(creationParams);
        outStore.createSchema(geoHashSchema); //Need to get the new schema somehow...

        SimpleFeatureStore featureStore = (SimpleFeatureStore) outStore.getFeatureSource(outFileName);
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

    /*
        This gives a bounding box, possibly expanded to fit a base32-representable hash (ie the number of bits is a multiple of 5.
        This method gives quite a bit of errors in the actual positions of the boxes,though.
     */
    public static ch.hsr.geohash.BoundingBox boundingBox2GeoHashBoundingBox(BoundingBox bounds) {
        ch.hsr.geohash.BoundingBox boundingBox = new ch.hsr.geohash.BoundingBox(bounds.getMinY(),
                bounds.getMaxY(),
                bounds.getMinX(),
                bounds.getMaxX());
        int fittingBits = GeoHashSizeTable.numberOfBitsForOverlappingGeoHash(boundingBox);
        WGS84Point center = boundingBox.getCenterPoint();
        GeoHash centerHash = GeoHash.withBitPrecision(center.getLatitude(),center.getLongitude(), fittingBits + (5 - (fittingBits % 5)));
        return centerHash.getBoundingBox();
    }

    public static SimpleFeatureCollection getFeaturesFromShape(String pathname, String cqlPredicate) throws IOException, CQLException {
        FileDataStore dataStore = FileDataStoreFinder.getDataStore(new File(pathname));
        SimpleFeatureSource featureSource = dataStore.getFeatureSource();
        Filter filter = CQL.toFilter(cqlPredicate);
        return featureSource.getFeatures(filter);
    }

    public static SimpleFeatureCollection getFeaturesFromShape(String pathname) throws IOException {
        FileDataStore dataStore = FileDataStoreFinder.getDataStore(new File(pathname));
        SimpleFeatureSource featureSource = dataStore.getFeatureSource();
        return featureSource.getFeatures();
    }


    public static Polygon createPolygonFromHash(String hash) {
        GeometryFactory factory = JTSFactoryFinder.getGeometryFactory();
        GeoHash geoHash = GeoHash.fromGeohashString(hash);

        ch.hsr.geohash.BoundingBox boundingBox = geoHash.getBoundingBox();
        Coordinate[] coordinates = CoordinateListFromBoundingBox(boundingBox);
        return factory.createPolygon(coordinates);
    }

    public static Polygon createPolygonFromHash(GeoHash hash) {
        GeometryFactory factory = JTSFactoryFinder.getGeometryFactory();
        ch.hsr.geohash.BoundingBox boundingBox = hash.getBoundingBox();
        Coordinate[] coordinates = CoordinateListFromBoundingBox(boundingBox);
        return factory.createPolygon(coordinates);

    }

    public static Coordinate[] CoordinateListFromBoundingBox(ch.hsr.geohash.BoundingBox boundingBox) {
        return new Coordinate[] {
                    new Coordinate(boundingBox.getMinLon(),boundingBox.getMinLat()),
                    new Coordinate(boundingBox.getMaxLon(),boundingBox.getMinLat()),
                    new Coordinate(boundingBox.getMaxLon(),boundingBox.getMaxLat()),
                    new Coordinate(boundingBox.getMinLon(),boundingBox.getMaxLat()),
                    new Coordinate(boundingBox.getMinLon(),boundingBox.getMinLat())};
    }

    public static SimpleFeatureType createGeohashScema() {
        SimpleFeatureTypeBuilder schemabuilder = new SimpleFeatureTypeBuilder();
        schemabuilder.setName("Test");
        schemabuilder.add("the_geom", Polygon.class);
        schemabuilder.length(7).add("geohash",String.class);
        return schemabuilder.buildFeatureType();
    }


}
