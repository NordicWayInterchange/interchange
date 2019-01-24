import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;

import java.io.File;
import java.io.IOException;

public class ShapeView {

    public static void main(String[] args) throws IOException {

        MapContent map = new MapContent();
        map.setTitle("Geohashing tiles for Norway w/10km buffer");
        if (args.length == 0) {
            //TODO not on my computer!
            map.addLayer(createLayer(new File("C:\\interchange\\shapefiler\\oresund3\\norway_shape.shp")));
            //map.addLayer(createLayer(new File("C:\\interchange\\shapefiler\\oresund3\\norway_outer_hashes_4.shp")));
            //map.addLayer(createLayer(new File("C:\\interchange\\shapefiler\\oresund3\\norway_inner_hashes_3.shp")));
            //map.addLayer(createLayer(new File("C:\\interchange\\shapefiler\\oresund3\\worldshape_10kmbuffer_oresund3.shp")));
            //map.addLayer(createLayer(new File("C:\\interchange\\shapefiler\\oresund3\\polygonlist_compressed.shp")));
            //map.addLayer(createLayer(new File("C:\\interchange\\shapefiler\\oresund3\\polygonlist_random_length_7.shp")));
            //map.addLayer(createLayer(new File("C:\\interchange\\shapefiler\\oresund3\\hashed_depth_5_rounds_400000.shp")));
            //map.addLayer(createLayer(new File("C:\\interchange\\shapefiler\\oresund3\\generated_shape_size_5.shp")));
            map.addLayer(createLayer(new File("C:\\interchange\\shapefiler\\oresund3\\hashes_5_compressed.shp")));
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
}
