import no.vegvesen.interchange.quadtree.QuadTreeTool;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import static org.junit.Assert.assertEquals;

public class QuadTreeTest {


    @Test
    public void testSomeWellKnownPlaces() {
        //Bouvet-kontoret
        double lat = 59.9304610;
        double lon = 10.7115718;
        int zoom = 14;
        assertEquals("12002131122311", QuadTreeTool.lonLatToQuadTree(lon,lat,zoom));
        //moomin world
        lat = 60.4732;
        lon = 22.0054;
        assertEquals("12003113303021",QuadTreeTool.lonLatToQuadTree(lon,lat,zoom));
        //Grøna lund
        lat = 59.3235075;
        lon = 18.0972829;
        assertEquals("12003122130131",QuadTreeTool.lonLatToQuadTree(lon,lat,zoom));
        //legoland
        lat = 55.7351130;
        lon = 9.1309000;
        assertEquals("12020110011133",QuadTreeTool.lonLatToQuadTree(lon,lat,zoom));
        //dyreparken
        lat = 58.1872898;
        lon = 8.1401363;
        assertEquals("12002301330230",QuadTreeTool.lonLatToQuadTree(lon,lat,zoom));
        //bouvet-øya
        lat = -54.418170;
        lon = 3.354422;
        assertEquals("30202230031022",QuadTreeTool.lonLatToQuadTree(lon,lat,zoom));
        //frihetsgudinnen
        lat = 40.6892;
        lon = -74.0445;
        assertEquals("03201011030112",QuadTreeTool.lonLatToQuadTree(lon,lat,zoom));
        //parque centario, Buenos Aires, Argentina
        lat = -34.697221;
        lon = -58.436691;
        assertEquals("21032130031302",QuadTreeTool.lonLatToQuadTree(lon,lat,zoom));
    }

    @Test
    public void testClip() {
        Assertions.assertEquals(180d,QuadTreeTool.clip(185d,-180d,180d),0.001d);
    }
}
