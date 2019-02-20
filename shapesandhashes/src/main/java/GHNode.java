import ch.hsr.geohash.GeoHash;

import java.util.*;

public class GHNode {
    private char currentChar;
    private Map<Character,GHNode> childMap;

    public GHNode(char currentChar) {
        this.currentChar = currentChar;
        this.childMap = new HashMap<>();
    }

    public Collection<GHNode> children() {
        return childMap.values();
    }

    public static GHNode fromHash(GeoHash hash) {
        String hashValue = hash.toBase32();
        GHNode res = new GHNode(hashValue.charAt(0));
        res.addChildren(hashValue.substring(1));
        return res;
    }

    public static GHNode fromHashes(Collection<GeoHash> hashes) {
        GHNode top = null;
        for (GeoHash hash : hashes) {
            if (top == null) {
                top = fromHash(hash);
            } else {
                top.add(hash);
            }
        }
        return top;
    }

    private void addChildren(String remainingHash) {
        if (remainingHash.length() == 0) {
            return;
        }
        char c = remainingHash.charAt(0);
        if (childMap.containsKey(c)) {
            GHNode child = childMap.get(c);
            child.addChildren(remainingHash.substring(1));
        } else {
            GHNode newChild = new GHNode(c);
            newChild.addChildren(remainingHash.substring(1));
            childMap.put(c,newChild);
        }

    }


    public char character() {
        return currentChar ;
    }

    public boolean isLeaf() {
        return childMap.isEmpty();
    }

    public void add(GeoHash hash) {
        String hashValue = hash.toBase32();
        if (currentChar != hashValue.charAt(0)) {
            throw new IllegalArgumentException("The initial hash character must match. Expected " + currentChar + ", got " + hashValue.charAt(0));
        }
        addChildren(hashValue.substring(1));
    }

    public boolean overlaps(GHNode other) {
        if (currentChar == other.currentChar) {
            if (this.isLeaf() || other.isLeaf()) {
                return true;
            }
            for (Character otherChildChar : other.childMap.keySet()) {
                if (childMap.containsKey(otherChildChar)) {
                    if (childMap.get(otherChildChar).overlaps(other.childMap.get(otherChildChar))) {
                        return true;
                    }
                }
            }
        } else {
            return false;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GHNode ghNode = (GHNode) o;
        return currentChar == ghNode.currentChar &&
                Objects.equals(childMap, ghNode.childMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentChar, childMap);
    }
}
