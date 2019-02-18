import ch.hsr.geohash.GeoHash;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class GNodeTest {
    private static class CountingGHNodeVisitor implements GHNodeVisitor {
        int depth = 0;

        @Override
        public void visit(GHNode node) {
            depth++;
            if (node.children().size() > 0) {
                visit(node.children().iterator().next());
            }
        }

        public int depth() {
            return depth;
        }
    }

    private static class OutputtingGHNodeVisitor implements GHNodeVisitor {
        private String current = "";
        private List<GeoHash> hashes = new ArrayList<>();

        @Override
        public void visit(GHNode node) {
            if (node.isLeaf()) {
                hashes.add(GeoHash.fromGeohashString(current + node.character()));
            } else {
                current += node.character();
                for (GHNode child : node.children()) {
                    visit(child);
                }
            }
        }

        public List<GeoHash> getHashes() {
            return hashes;
        }
    }

    @Test
    public void testSimpleGHNode() {
        String testString = "u4xs";
        GeoHash g = GeoHash.fromGeohashString(testString);
        GHNode ghNode = GHNode.fromHash(g);
        CountingGHNodeVisitor depthVisitor = new CountingGHNodeVisitor();
        depthVisitor.visit(ghNode);
        assertThat(depthVisitor.depth()).isEqualTo(testString.length());
    }

    @Test
    public void mergeTwoGeohashesIntOneTree() {
        String string1 = "u4xs";
        String string2 = "u4xu";
        GeoHash g1 = GeoHash.fromGeohashString(string1);
        GeoHash g2 = GeoHash.fromGeohashString(string2);
        GHNode topNode = GHNode.fromHash(g1);
        topNode.add(g2);
        OutputtingGHNodeVisitor visitor = new OutputtingGHNodeVisitor();
        visitor.visit(topNode);
        List<GeoHash> hashes = visitor.getHashes();
        assertThat(hashes.size()).isEqualTo(2);
    }

    @Test
    public void compareTwoTreesWithTwoDifferentGeohash() {
        String string1 = "u4xs";
        String string2 = "u4xu";
        GeoHash g1 = GeoHash.fromGeohashString(string1);
        GeoHash g2 = GeoHash.fromGeohashString(string2);
        GHNode gh1 = GHNode.fromHash(g1);
        GHNode gh2 = GHNode.fromHash(g2);
        //assertThat(GHNode.overlaps(gh1,gh2)).isFalse();

        assertThat(gh1.overlaps(gh2)).isFalse();

    }

    @Test
    public void compareTwoTreesWithSameGeohash() {
        String string1 = "u4xs";
        GeoHash g1 = GeoHash.fromGeohashString(string1);
        GeoHash g2 = GeoHash.fromGeohashString(string1);
        GHNode gh1 = GHNode.fromHash(g1);
        GHNode gh2 = GHNode.fromHash(g2);
        assertThat(gh1.overlaps(gh2)).isTrue();
        assertThat(gh2.overlaps(gh1)).isTrue();
    }

    @Test
    public void sameAreaOverlaps() {
        GHNode first = GHNode.fromHash(GeoHash.fromGeohashString("u705"));
        assertThat(first.overlaps(first)).isTrue();
    }

    @Test
    public void overAreaWithOneCommonHashOverlaps() {
        GHNode first = GHNode.fromHash(GeoHash.fromGeohashString("u705"));
        first.add(GeoHash.fromGeohashString("u70h"));
        GHNode other = GHNode.fromHash(GeoHash.fromGeohashString("u705"));
        other.add(GeoHash.fromGeohashString("u70x"));
        assertThat(first.overlaps(other)).isTrue();
        assertThat(other.overlaps(first)).isTrue();
    }

    @Test
    public void overAreaWithNoCommonHashDoesNotOverlap() {
        GHNode first = GHNode.fromHash(GeoHash.fromGeohashString("u705"));
        first.add(GeoHash.fromGeohashString("u70h"));

        GHNode other = GHNode.fromHash(GeoHash.fromGeohashString("u70z"));
        other.add(GeoHash.fromGeohashString("u70x"));
        assertThat(first.overlaps(other)).isFalse();
        assertThat(other.overlaps(first)).isFalse();
    }

    @Test
    public void otherAreaWithOneSubHashOverlaps() {
        GHNode first = GHNode.fromHash(GeoHash.fromGeohashString("u705"));
        first.add(GeoHash.fromGeohashString("u70h"));

        GHNode other = GHNode.fromHash(GeoHash.fromGeohashString("u705f"));
        other.add(GeoHash.fromGeohashString("u70h4"));


        assertThat(first.overlaps(other)).isTrue();
        assertThat(other.overlaps(first)).isTrue();

    }
}
