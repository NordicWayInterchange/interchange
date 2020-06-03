package no.vegvesen.interchange.quadtree;

/*-
 * #%L
 * quadtree-tool
 * %%
 * Copyright (C) 2019 - 2020 Nordic Way 3
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class QuadTreeTest {


    @Test
    public void testSomeWellKnownPlaces() {
        //Bouvet-kontoret
        double lat = 59.9304610;
        double lon = 10.7115718;
        int zoom = 14;
        assertThat(QuadTreeTool.lonLatToQuadTree(lon,lat,zoom)).isEqualTo("12002131122311");
        //moomin world
        lat = 60.4732;
        lon = 22.0054;
        assertThat(QuadTreeTool.lonLatToQuadTree(lon,lat,zoom)).isEqualTo("12003113303021");
        //Grøna lund
        lat = 59.3235075;
        lon = 18.0972829;
        assertThat(QuadTreeTool.lonLatToQuadTree(lon,lat,zoom)).isEqualTo("12003122130131");
        //legoland
        lat = 55.7351130;
        lon = 9.1309000;
        assertThat(QuadTreeTool.lonLatToQuadTree(lon,lat,zoom)).isEqualTo("12020110011133");
        //dyreparken
        lat = 58.1872898;
        lon = 8.1401363;
        assertThat(QuadTreeTool.lonLatToQuadTree(lon,lat,zoom)).isEqualTo("12002301330230");
        //bouvet-øya
        lat = -54.418170;
        lon = 3.354422;
        assertThat(QuadTreeTool.lonLatToQuadTree(lon,lat,zoom)).isEqualTo("30202230031022");
        //frihetsgudinnen
        lat = 40.6892;
        lon = -74.0445;
        assertThat(QuadTreeTool.lonLatToQuadTree(lon,lat,zoom)).isEqualTo("03201011030112");
        //parque centario, Buenos Aires, Argentina
        lat = -34.697221;
        lon = -58.436691;
        assertThat(QuadTreeTool.lonLatToQuadTree(lon,lat,zoom)).isEqualTo("21032130031302");
    }

    @Test
    public void testClip() {
        assertThat(QuadTreeTool.clip(185d,-180d,180d))
                .isEqualTo(180d, Offset.offset(0.001d));
    }
}
