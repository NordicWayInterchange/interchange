import ch.hsr.geohash.GeoHash;

import java.util.*;

public class GHNode {
    private char currentChar;
    private Set<GHNode> children;

    public GHNode(char currentChar) {
        this.children = new HashSet<>();
        this.currentChar = currentChar;
    }

    public Set<GHNode> children() {
        return children;
    }

    public static GHNode fromHash(GeoHash hash) {
        String hashValue = hash.toBase32();
        GHNode res = new GHNode(hashValue.charAt(0));
        GHNode current = res;
        for (int i = 1; i < hashValue.length(); i++) {
            char currentChar = hashValue.charAt(i);
            GHNode n = new GHNode(currentChar);
            current.children.add(n);
            current = n;
        }
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
        boolean found = false;
        for (GHNode child : children) {
           if (child.currentChar == c ) {
               found = true;
               child.addChildren(remainingHash.substring(1));
           }
        }
        if (! found) {
            GHNode newChild = new GHNode(c);
            newChild.addChildren(remainingHash.substring(1));
            children.add(newChild);
        }

    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GHNode ghNode = (GHNode) o;
        return currentChar == ghNode.currentChar &&
                Objects.equals(children, ghNode.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentChar, children);
    }


    public char character() {
        return currentChar ;
    }

    public boolean isLeaf() {
        return children.size() == 0;
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
            for (GHNode otherChild : other.children) {
                //Find the child with matching character. TODO should I repurpose the Set to a Hash?
                final char otherChar = otherChild.currentChar;
                Optional<GHNode> first = children.stream().filter(node -> node.currentChar == otherChar).findFirst();
                if (first.isPresent()) {
                    //visit the child with the other child
                    if (first.get().overlaps(otherChild)) {
                        return true;
                    }
                }
            }
        } else {
            return false;
        }
        return false;
    }
}
