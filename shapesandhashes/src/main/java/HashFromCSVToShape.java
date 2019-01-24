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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HashFromCSVToShape {


    public static void main(String[] args) throws IOException {
        final SimpleFeatureType geoHashSchema = ShapeHasher.createGeohashScema();

        System.out.println(geoHashSchema.toString());

        List<SimpleFeature> polygonList = new ArrayList<>();
        String input_file = "hashes_5_compressed.csv";
        for (String hash : Files.readAllLines(Paths.get("C:\\interchange\\shapefiler\\oresund3", input_file))) {
            Polygon polygonFromHash = ShapeHasher.createPolygonFromHash(hash);
            SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(geoHashSchema);
            featureBuilder.add(polygonFromHash);
            featureBuilder.add(hash);
            polygonList.add(featureBuilder.buildFeature(null));
        }



        String filename = "hashes_5_compressed";
        File outFile = new File("C:\\interchange\\shapefiler\\oresund3\\" + filename + ".shp");
        Map<String, Serializable> creationParams = new HashMap<>();
        creationParams.put("url", URLs.fileToUrl(outFile));
        FileDataStoreFactorySpi factory = FileDataStoreFinder.getDataStoreFactory("shp");
        DataStore outStore = factory.createDataStore(creationParams);
        outStore.createSchema(geoHashSchema); //Need to get the new schema somehow...

        SimpleFeatureStore featureStore = (SimpleFeatureStore) outStore.getFeatureSource(filename);
        SimpleFeatureCollection collection = new ListFeatureCollection(geoHashSchema, polygonList);

        try (Transaction t = new DefaultTransaction()) {
            featureStore.addFeatures(collection);
            t.commit();
        }

    }

}
