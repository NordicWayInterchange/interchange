package no.vegvesen.ixn.federation.capability.quadtree;

public class QuadTree {

    public static class LatLon {
        double lat;
        double lon;

        LatLon(double latitude, double longitude) {
            lat = latitude;
            lon = longitude;
        }

        @Override
        public String toString() {
            return lat+" "+lon;
        }
    }

    private final static double EARTH_RADIUS = 6378137;
    private final static double MIN_LAT = -85.05112878;
    private final static double MAX_LAT = 85.05112878;
    private final static double MIN_LON = -180;
    private final static double MAX_LON = 180;

    /// <summary>
    /// Clips a number to the specified minimum and maximum values.
    /// </summary>
    /// <param name="n">The number to clip.</param>
    /// <param name="minValue">Minimum allowable value.</param>
    /// <param name="maxValue">Maximum allowable value.</param>
    /// <returns>The clipped value.</returns>
    private static double clip(double n, double minValue, double maxValue)
    {
        return Math.min(Math.max(n, minValue), maxValue);
    }

    /// <summary>
    /// Determines the map width and height (in pixels) at a specified level
    /// of detail.
    /// </summary>
    /// <param name="levelOfDetail">Level of detail, from 1 (lowest detail)
    /// to 23 (highest detail).</param>
    /// <returns>The map width and height in pixels.</returns>
    public static int mapSize(int levelOfDetail)
    {
        return 256 << levelOfDetail;
    }

    /// <summary>
    /// Determines the ground resolution (in meters per pixel) at a specified
    /// latitude and level of detail.
    /// </summary>
    /// <param name="latitude">Latitude (in degrees) at which to measure the
    /// ground resolution.</param>
    /// <param name="levelOfDetail">Level of detail, from 1 (lowest detail)
    /// to 23 (highest detail).</param>
    /// <returns>The ground resolution, in meters per pixel.</returns>
    public static double groundResolution(double latitude, int levelOfDetail)
    {
        latitude = clip(latitude, MIN_LAT, MAX_LAT);
        return Math.cos(latitude * Math.PI / 180) * 2 * Math.PI * EARTH_RADIUS / mapSize(levelOfDetail);
    }

    /// <summary>
    /// Determines the map scale at a specified latitude, level of detail,
    /// and screen resolution.
    /// </summary>
    /// <param name="latitude">Latitude (in degrees) at which to measure the
    /// map scale.</param>
    /// <param name="levelOfDetail">Level of detail, from 1 (lowest detail)
    /// to 23 (highest detail).</param>
    /// <param name="screenDpi">Resolution of the screen, in dots per inch.</param>
    /// <returns>The map scale, expressed as the denominator N of the ratio 1 : N.</returns>
    public static double mapScale(double latitude, int levelOfDetail, int screenDpi)
    {
        return groundResolution(latitude, levelOfDetail) * screenDpi / 0.0254;
    }

    public static String latLonToQuadtree(double latitude, double longitude, int levelOfDetail) {
        latitude = clip(latitude, MIN_LAT, MAX_LAT);
        longitude = clip(longitude, MIN_LON, MAX_LON);

        double x = (longitude + 180) / 360;
        double sinLatitude = Math.sin(latitude * Math.PI / 180);
        double y = 0.5 - Math.log((1 + sinLatitude) / (1 - sinLatitude)) / (4 * Math.PI);

        int mapSize = mapSize(levelOfDetail);
        int tileX = (int) clip(x * mapSize + 0.5, 0, mapSize - 1)/256;
        int tileY = (int) clip(y * mapSize + 0.5, 0, mapSize - 1)/256;

        StringBuilder quadKey = new StringBuilder();
        for (int i = levelOfDetail; i > 0; i--)
        {
            char digit = '0';
            int mask = 1 << (i - 1);
            if ((tileX & mask) != 0)
            {
                digit++;
            }
            if ((tileY & mask) != 0)
            {
                digit++;
                digit++;
            }
            quadKey.append(digit);
        }
        return quadKey.toString();
    }

    public static LatLon quadtreeToLatLon(String quadKey){
        int tileX = 0;
        int tileY = 0;
        int levelOfDetail = quadKey.length();
        for (int i = levelOfDetail; i > 0; i--)
        {
            int mask = 1 << (i - 1);
            switch (quadKey.charAt(levelOfDetail - i))
            {
                case '0':
                    break;

                case '1':
                    tileX |= mask;
                    break;

                case '2':
                    tileY |= mask;
                    break;

                case '3':
                    tileX |= mask;
                    tileY |= mask;
                    break;

                default:
                    throw new IllegalArgumentException("Invalid QuadKey digit sequence.");
            }
        }

        int pixelX = tileX*256;
        int pixelY = tileY*256;

        double mapSize = mapSize(levelOfDetail);
        double x = (clip(pixelX, 0, mapSize - 1) / mapSize) - 0.5;
        double y = 0.5 - (clip(pixelY, 0, mapSize - 1) / mapSize);

        double latitude = 90 - 360 * Math.atan(Math.exp(-y * 2 * Math.PI)) / Math.PI;
        double longitude = 360 * x;

        return new LatLon(latitude,longitude);

    }

    public static void main(String[] args) {
        String qt = latLonToQuadtree(64.4, 10.4, 18);
        System.out.println("QT: "+qt);
        LatLon ll = quadtreeToLatLon(qt);
        System.out.println(ll);
        String[] tiles = QuadTreeHelper.getNeighbours(qt);
        for (String t : tiles) {
            System.out.println(t);
        }
    }
}
