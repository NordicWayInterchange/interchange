package no.vegvesen.ixn.federation.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class Selector {


    @Column(columnDefinition="TEXT")
    private String selector;

    public Selector() {
    }

    public Selector(String selector) {
        this.selector = selector;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Selector selector1 = (Selector) o;
        return Objects.equals(selector, selector1.selector);
    }

    @Override
    public int hashCode() {
        return Objects.hash(selector);
    }
}
