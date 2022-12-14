package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.federation.api.v1_0.DatexCapabilityApi;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class CapabilityValidatorTest {

    static final String publisherId = "service-provider-1";
    static final String originatingCountry = "NO";
    static final String protocolVersion = "1.0";
    static final Set<String> quadTree = new HashSet<>(Arrays.asList("0123", "0122"));

    @Test
    public void validateCapabilityIsOk() {
        DatexCapabilityApi capability = new DatexCapabilityApi();
        capability.setPublisherId(publisherId);
        capability.setOriginatingCountry(originatingCountry);
        capability.setProtocolVersion(protocolVersion);
        capability.setQuadTree(quadTree);

        assertThat(CapabilityValidator.validateCapability(capability)).isTrue();
    }

    @Test
    public void validatePublisherIdPropertyIsMissing() {
        DatexCapabilityApi capability = new DatexCapabilityApi();
        capability.setMessageType(Constants.DATEX_2);
        capability.setOriginatingCountry(originatingCountry);
        capability.setProtocolVersion(protocolVersion);
        capability.setQuadTree(quadTree);

        assertThat(CapabilityValidator.validateCapability(capability)).isFalse();
    }

    @Test
    public void validateOriginatingCountryPropertyIsMissing() {
        DatexCapabilityApi capability = new DatexCapabilityApi();
        capability.setMessageType(Constants.DATEX_2);
        capability.setPublisherId(publisherId);
        capability.setProtocolVersion(protocolVersion);
        capability.setQuadTree(quadTree);

        assertThat(CapabilityValidator.validateCapability(capability)).isFalse();
    }

    @Test
    public void validateProtocolVersionPropertyIsMissing() {
        DatexCapabilityApi capability = new DatexCapabilityApi();
        capability.setMessageType(Constants.DATEX_2);
        capability.setPublisherId(publisherId);
        capability.setOriginatingCountry(originatingCountry);
        capability.setQuadTree(quadTree);

        assertThat(CapabilityValidator.validateCapability(capability)).isFalse();
    }

    @Test
    public void validateQuadTreePropertyIsMissing() {
        DatexCapabilityApi capability = new DatexCapabilityApi();
        capability.setMessageType(Constants.DATEX_2);
        capability.setPublisherId(publisherId);
        capability.setOriginatingCountry(originatingCountry);
        capability.setProtocolVersion(protocolVersion);

        assertThat(CapabilityValidator.validateCapability(capability)).isFalse();
    }

}
