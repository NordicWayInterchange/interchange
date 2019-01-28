import org.geotools.data.*;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.util.URLs;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class WorldToCountryShape {


    public static void main(String[] args) throws IOException, CQLException {
        String pathname = "C:\\interchange\\shapefiler\\oresund3\\worldshape_10kmbuffer_oresund3.shp";
        String cqlPredicate = "ISO2 = 'DK'";
        FileDataStore dataStore = FileDataStoreFinder.getDataStore(new File(pathname));
        SimpleFeatureSource featureSource = dataStore.getFeatureSource();
        Filter filter = CQL.toFilter(cqlPredicate);
        SimpleFeatureType schema = featureSource.getSchema();
        SimpleFeatureCollection features = featureSource.getFeatures(filter);
        String fileBase = "Denmark_10km_buffer";
        String outPath = "C:\\interchange\\shapefiler\\oresund3\\" + fileBase + ".shp";
        Map<String, Serializable> creationParams = new HashMap<>();
        creationParams.put("url", URLs.fileToUrl(new File(outPath)));
        FileDataStoreFactorySpi factory = FileDataStoreFinder.getDataStoreFactory(".shp");
        DataStore outStore = factory.createDataStore(creationParams);
        outStore.createSchema(schema);

        SimpleFeatureStore featureStore = (SimpleFeatureStore)outStore.getFeatureSource(fileBase);


        Transaction t = new DefaultTransaction();
        try {
            featureStore.addFeatures(features);
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
}
