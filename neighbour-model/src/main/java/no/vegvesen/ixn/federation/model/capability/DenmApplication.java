package no.vegvesen.ixn.federation.model.capability;

import no.vegvesen.ixn.federation.api.v1_0.capability.ApplicationApi;
import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.federation.api.v1_0.capability.DenmApplicationApi;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@DiscriminatorValue(Constants.DENM)
public class DenmApplication extends Application{

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "application_causecodes", joinColumns = @JoinColumn(name = "app_id", foreignKey = @ForeignKey(name="fk_appcac_cap")))
    @Column(name = "cause_codes")
    private Set<Integer> causeCodes = new HashSet<>();

    public DenmApplication() {

    }

    public DenmApplication(String publisherId, String publicationId, String originatingCountry, String protocolVersion, Set<String> quadTree, Set<Integer> causeCodes) {
        super(publisherId, publicationId, originatingCountry, protocolVersion, quadTree);
        this.causeCodes.addAll(causeCodes);
    }

    public Set<Integer> getCauseCodes() {
        return causeCodes;
    }

    public void setCauseCodes(Set<Integer> causeCodes) {
        this.causeCodes = causeCodes;
    }

    @Override
    public ApplicationApi toApi() {
        return new DenmApplicationApi(getPublisherId(), getPublicationId(), getOriginatingCountry(), getProtocolVersion(), getQuadTree(), getCauseCodes());
    }

    @Override
    public String messageType() {
        return Constants.DENM;
    }
}
