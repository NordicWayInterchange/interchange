package no.vegvesen.ixn.federation.capability;
import no.vegvesen.ixn.federation.api.v1_0.*;
import org.springframework.stereotype.Component;

@Component
public class CapabilityVerifier {

    public boolean verifyCapability(CapabilityApi capability) {
        boolean capabilityIsVerified = false;
        if (capability instanceof DatexCapabilityApi) {

        }
        else if (capability instanceof DenmCapabilityApi) {

        }
        else if (capability instanceof IvimCapabilityApi) {

        }
        else if (capability instanceof SpatemCapabilityApi) {

        }
        else if (capability instanceof MapemCapabilityApi) {

        }
        else if (capability instanceof SremCapabilityApi) {

        }
        else if (capability instanceof SsemCapabilityApi) {

        }
        else if (capability instanceof CamCapabilityApi) {

        }
        return capabilityIsVerified;
    }

    public boolean checkCommonProperties(CapabilityApi capability) {
        for (String property : capability.getCommonProperties().values()) {
            if (property == null) {
                return false;
            }
        }
        return true;
    }
}
