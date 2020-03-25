/**
 * This class contains code that has been ported from the geotools project,
 * which has further based its implementation on <a
 * href="https://msdn.microsoft.com/en-us/library/bb259689.aspx>Bing Maps</a>
 */
public class QuadTreeTool {

    public static final double MAX_LATITUDE = 85.05112878;
    public static final double MIN_LATITUDE = -85.05112878;
    public static final int MIN_LONGITUDE = -180;
    public static final int MAX_LONGITUDE = 180;

    public static String lonLatToQuadTree(double lon, double lat, int zoom) {
        //lon lat to pixelXY
        double latitude = clip(lat, MIN_LATITUDE,MAX_LATITUDE);
        double longitude = clip(lon, MIN_LONGITUDE, MAX_LONGITUDE);

        double x = (longitude + 180) / 360;

        double sinLatitude = Math.sin(latitude * Math.PI / 180);

        double log = Math.log((1 + sinLatitude) / (1 - sinLatitude));
        double fourPi = 4 * Math.PI;
        double y = 0.5 - (log / fourPi);

        //256 * 2^zoom:
        int mapSize = 256 << zoom;

        double scaledX = x * mapSize;
        double scaledY = y * mapSize;
        int pixelX = (int) clip(scaledX + 0.5,0,mapSize - 1);
        int pixelY = (int) clip(scaledY + 0.5,0, mapSize - 1);

        //pixelXY to tile X Y
        int tileX = pixelX / 256;
        int tileY = pixelY / 256;

        //tile X Y to quad tree
        StringBuilder quadKey = new StringBuilder();
        for (int i = zoom; i > 0; i--) {
            char digit = '0';
            int mask = 1 << (i - 1);
            if ((tileX & mask) != 0) {
                digit++;
            }
            if ((tileY & mask) != 0) {
                digit++;
                digit++;
            }
            quadKey.append(digit);
        }
        return quadKey.toString();

    }


    static double clip(double n, double minValue, double maxValue) {
        return Math.min(Math.max(n,minValue),maxValue);
    }

}
