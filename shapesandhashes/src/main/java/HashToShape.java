import org.geotools.data.*;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.util.URLs;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HashToShape {

    private static String[] hashes = new String[] {"u4xt","u4xw","u4xsmp","u4xsmr","u4xsmx","u4xsmz","u4xsmn","u4xsmq","u4xsmw","u4xsmy","u4xsmj","u4xsmm","u4xsmt","u4xsmv","u4xsmh","u4xsmk","u4xsms","u4xsmu","u4xsm5","u4xsm7","u4xsme","u4xsmg","u4xsmd","u4xs"};

    public static void main(String[] args) throws IOException {
        final SimpleFeatureType geoHashSchema = ShapeHasher.createGeohashScema();

        System.out.println(geoHashSchema.toString());

        List<SimpleFeature> polygonList = new ArrayList<>();
        for (String hash : hashes) {
            Polygon polygonFromHash = ShapeHasher.createPolygonFromHash(hash);
            SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(geoHashSchema);
            featureBuilder.add(polygonFromHash);
            featureBuilder.add(hash);
            polygonList.add(featureBuilder.buildFeature(null));
        }
        String fileName = "oslo_4_length";
        createShapeFromPolygons(geoHashSchema, polygonList, fileName);

    }

    public static void createShapeFromPolygons(SimpleFeatureType geoHashSchema, List<SimpleFeature> polygonList, String fileName) throws IOException {
        File outFile = new File("C:\\interchange\\shapefiler\\oresund3\\" + fileName + ".shp");
        Map<String, Serializable> creationParams = new HashMap<>();
        creationParams.put("url", URLs.fileToUrl(outFile));
        FileDataStoreFactorySpi factory = FileDataStoreFinder.getDataStoreFactory("shp");
        DataStore outStore = factory.createDataStore(creationParams);
        outStore.createSchema(geoHashSchema); //Need to get the new schema somehow...

        SimpleFeatureStore featureStore = (SimpleFeatureStore) outStore.getFeatureSource(fileName);
        SimpleFeatureCollection collection = new ListFeatureCollection(geoHashSchema, polygonList);

        try (Transaction t = new DefaultTransaction()) {
            featureStore.addFeatures(collection);
            t.commit();
        }
    }
}
