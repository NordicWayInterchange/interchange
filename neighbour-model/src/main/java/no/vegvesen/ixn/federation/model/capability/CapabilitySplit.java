package no.vegvesen.ixn.federation.model.capability;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "split_capability")
public class CapabilitySplit {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cap_plit_seq")
    private Integer id;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "app", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_cap_app"))
    private Application application;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "meta", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_cap_meta"))
    private Metadata metadata;

    @Enumerated(EnumType.STRING)
    private CapabilityStatus status = CapabilityStatus.CREATED;

    private String capabilityExchangeName = "";

    public CapabilitySplit() {

    }

    public CapabilitySplit(Application application, Metadata metadata) {
        this.application = application;
        this.metadata = metadata;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public CapabilityStatus getStatus() {
        return status;
    }

    public void setStatus(CapabilityStatus status) {
        this.status = status;
    }

    public String getCapabilityExchangeName() {
        return capabilityExchangeName;
    }

    public void setCapabilityExchangeName(String capabilityExchangeName) {
        this.capabilityExchangeName = capabilityExchangeName;
    }

    public boolean exchangeExists() {
        return !capabilityExchangeName.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CapabilitySplit that = (CapabilitySplit) o;
        return Objects.equals(application, that.application) && status == that.status && Objects.equals(capabilityExchangeName, that.capabilityExchangeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(application, status, capabilityExchangeName);
    }

    @Override
    public String toString() {
        return "CapabilitySplit{" +
                "id=" + id +
                ", application=" + application +
                ", metadata=" + metadata +
                ", status=" + status +
                ", capabilityExchangeName='" + capabilityExchangeName + '\'' +
                '}';
    }
}
