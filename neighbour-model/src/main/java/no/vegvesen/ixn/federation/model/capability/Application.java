package no.vegvesen.ixn.federation.model.capability;

import no.vegvesen.ixn.federation.api.v1_0.capability.ApplicationApi;

import jakarta.persistence.*;
import java.util.*;

@Entity()
@Table(name = "application")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "messageType")
public abstract class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "app_seq")
    private Integer id;

    private String publisherId;

    private String publicationId;

    private String originatingCountry;

    private String protocolVersion;

    @ElementCollection(fetch = FetchType.EAGER, targetClass = String.class)
    @CollectionTable(name = "app_quad", joinColumns = @JoinColumn(name = "app_id", nullable = false, foreignKey = @ForeignKey(name="fk_quad_app")))
    @Column(name = "quadrant_app", nullable = false)
    private final List<String> quadTree = new ArrayList<>();

    public Application() {

    }

    public Application(String publisherId, String publicationId, String originatingCountry, String protocolVersion, Set<String> quadTree) {
        this.publisherId = publisherId;
        this.publicationId = publicationId;
        this.originatingCountry = originatingCountry;
        this.protocolVersion = protocolVersion;
        this.quadTree.addAll(quadTree);
    }


    public String getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(String publisherId) {
        this.publisherId = publisherId;
    }

    public String getPublicationId() {
        return publicationId;
    }

    public void setPublicationId(String publicationId) {
        this.publicationId = publicationId;
    }

    public String getOriginatingCountry() {
        return originatingCountry;
    }

    public void setOriginatingCountry(String originatingCountry) {
        this.originatingCountry = originatingCountry;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public List<String> getQuadTree() {
        return quadTree;
    }

    public void setQuadTree(Set<String> quadTree) {
        this.quadTree.clear();
        if (quadTree != null){
            this.quadTree.addAll(quadTree);
        }
    }

    public abstract ApplicationApi toApi();

    public abstract String getMessageType();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Application that = (Application) o;

        return Objects.equals(publisherId, that.publisherId) && Objects.equals(publicationId, that.publicationId)
                && Objects.equals(originatingCountry, that.originatingCountry)
                && Objects.equals(protocolVersion, that.protocolVersion);

    }

    @Override
    public int hashCode() {
        return Objects.hash(
                publisherId,
                publicationId,
                originatingCountry,
                protocolVersion);
    }

    @Override
    public String toString() {
        return "Application{" +
                "publisherId='" + publisherId + '\'' +
                ", publicationId='" + publicationId + '\'' +
                ", originatingCountry='" + originatingCountry + '\'' +
                ", protocolVersion='" + protocolVersion + '\'' +
                ", quadTree=" + quadTree +
                '}';
    }
}
