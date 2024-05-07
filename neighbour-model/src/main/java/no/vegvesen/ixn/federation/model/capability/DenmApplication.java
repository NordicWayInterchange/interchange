package no.vegvesen.ixn.federation.model.capability;

import no.vegvesen.ixn.federation.api.v1_0.capability.ApplicationApi;
import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.federation.api.v1_0.capability.DenmApplicationApi;

import jakarta.persistence.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@DiscriminatorValue(Constants.DENM)
public class DenmApplication extends Application{

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "application_causecodes", joinColumns = @JoinColumn(name = "app_id", foreignKey = @ForeignKey(name="fk_appcac_cap")))
    @Column(name = "cause_codes")
    private Set<Integer> causeCode = new HashSet<>();

    public DenmApplication() {

    }

    public DenmApplication(String publisherId, String publicationId, String originatingCountry, String protocolVersion, Set<String> quadTree, Set<Integer> causeCode) {
        super(publisherId, publicationId, originatingCountry, protocolVersion, quadTree);
        this.causeCode.addAll(causeCode);
    }

    public DenmApplication(int id, String publisherId, String publicationId, String countryCode, String protocolVersion, Set<String> quadTree, Set<Integer> causeCode) {
       super(id, publisherId, publicationId, countryCode, protocolVersion, quadTree);
       this.causeCode.addAll(causeCode);
    }

    public Set<Integer> getCauseCode() {
        return causeCode;
    }

    public void setCauseCode(Set<Integer> causeCode) {
        this.causeCode = causeCode;
    }

    @Override
    public ApplicationApi toApi() {
        return new DenmApplicationApi(getPublisherId(), getPublicationId(), getOriginatingCountry(), getProtocolVersion(), getQuadTree(), getCauseCode());
    }

    @Override
    public String getMessageType() {
        return Constants.DENM;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DenmApplication that = (DenmApplication) o;
        return Objects.equals(Set.copyOf(causeCode), Set.copyOf(that.causeCode));
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), Set.copyOf(causeCode));
    }
}
