import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.File;
import java.io.IOException;

public class ShapeFileSchemaView {

    public static void main(String[] args) throws IOException {

        File file;
        if (args.length == 0) {
            //file = new File("C:\\interchange\\shapefiler\\oresund3\\kyrre_tester_linjestykke.shp");
            file = new File("C:\\interchange\\datex_eksempler\\linestrings_getSituation_1550841672078.shp");
        } else {
            file = new File(args[0]);
        }
        FileDataStore dataStore = FileDataStoreFinder.getDataStore(file);
        SimpleFeatureSource featureSource = dataStore.getFeatureSource();
        SimpleFeatureType schema = featureSource.getSchema();
        CoordinateReferenceSystem coordinateReferenceSystem = schema.getCoordinateReferenceSystem();
        System.out.println(coordinateReferenceSystem.getName());
        System.out.println(coordinateReferenceSystem.toWKT());
        System.out.println(coordinateReferenceSystem.getCoordinateSystem().getName());
        for (int i = 0; i < schema.getAttributeCount(); i++) {
            AttributeType type = schema.getType(i);
            AttributeDescriptor descriptor = schema.getDescriptor(i);
            System.out.println("type: " + type + " :-> " + descriptor);

        }
        SimpleFeatureCollection features = featureSource.getFeatures();
        System.out.println(String.format("Contains %d features",features.size()));

    }
}
