import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class ShapeView {

    public static void main(String[] args) throws IOException {

        MapContent map = new MapContent();
        map.setTitle("Nordic countries w/10km buffer");
        if (args.length == 0) {
            //TODO not on my computer!
            map.addLayer(createLayer(new File("C:\\interchange\\shapefiler\\oresund3\\norway_shape.shp"),Color.RED));
            map.addLayer(createLayer(new File("C:\\interchange\\shapefiler\\oresund3\\oslo_4_length.shp")));
        } else {
            for (String filename : args) {
                map.addLayer(createLayer(new File(filename)));
            }
        }
        JMapFrame.showMap(map);
    }

    public static Layer createLayer(File file) throws IOException {
        FileDataStore dataStore = FileDataStoreFinder.getDataStore(file);
        SimpleFeatureSource featureSource = dataStore.getFeatureSource();
        Style style = SLD.createSimpleStyle(featureSource.getSchema());
        return new FeatureLayer(featureSource,style);

    }

    public static Layer createLayer(File file, Color lineColor) throws IOException {
        FileDataStore dataStore = FileDataStoreFinder.getDataStore(file);
        SimpleFeatureSource featureSource = dataStore.getFeatureSource();
        Style style = SLD.createPolygonStyle(lineColor,lineColor,0.2f);
        return new FeatureLayer(featureSource,style);

    }

}
