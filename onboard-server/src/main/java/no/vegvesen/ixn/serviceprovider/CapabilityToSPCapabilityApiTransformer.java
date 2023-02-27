package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.serviceprovider.capability.*;
import no.vegvesen.ixn.federation.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CapabilityToSPCapabilityApiTransformer {

    private static Logger logger = LoggerFactory.getLogger(CapabilityToSPCapabilityApiTransformer.class);

    public CapabilityToSPCapabilityApiTransformer() {

    }

    public Capability capabilityApiToCapability(SPCapabilityApi SPCapabilityApi) {
        if (SPCapabilityApi instanceof DatexSPCapabilityApi){
            return new DatexCapability(SPCapabilityApi.getPublisherId(), SPCapabilityApi.getOriginatingCountry(), SPCapabilityApi.getProtocolVersion(), SPCapabilityApi.getQuadTree(), transformRedirectStatusApiToRedirectStatus(SPCapabilityApi.getRedirect()), ((DatexSPCapabilityApi) SPCapabilityApi).getPublicationType());
        }
        else if (SPCapabilityApi instanceof DenmSPCapabilityApi) {
            return new DenmCapability(SPCapabilityApi.getPublisherId(), SPCapabilityApi.getOriginatingCountry(), SPCapabilityApi.getProtocolVersion(), SPCapabilityApi.getQuadTree(), transformRedirectStatusApiToRedirectStatus(SPCapabilityApi.getRedirect()), ((DenmSPCapabilityApi) SPCapabilityApi).getCauseCode());
        }
        else if (SPCapabilityApi instanceof IvimSPCapabilityApi) {
            return new IvimCapability(SPCapabilityApi.getPublisherId(), SPCapabilityApi.getOriginatingCountry(), SPCapabilityApi.getProtocolVersion(), SPCapabilityApi.getQuadTree(), transformRedirectStatusApiToRedirectStatus(SPCapabilityApi.getRedirect()), ((IvimSPCapabilityApi) SPCapabilityApi).getIviType());
        }
        else if (SPCapabilityApi instanceof SpatemSPCapabilityApi) {
            return new SpatemCapability(SPCapabilityApi.getPublisherId(), SPCapabilityApi.getOriginatingCountry(), SPCapabilityApi.getProtocolVersion(), SPCapabilityApi.getQuadTree(), transformRedirectStatusApiToRedirectStatus(SPCapabilityApi.getRedirect()), ((SpatemSPCapabilityApi) SPCapabilityApi).getIds());
        }
        else if (SPCapabilityApi instanceof MapemSPCapabilityApi) {
            return new MapemCapability(SPCapabilityApi.getPublisherId(), SPCapabilityApi.getOriginatingCountry(), SPCapabilityApi.getProtocolVersion(), SPCapabilityApi.getQuadTree(), transformRedirectStatusApiToRedirectStatus(SPCapabilityApi.getRedirect()), ((MapemSPCapabilityApi) SPCapabilityApi).getIds());
        }
        else if (SPCapabilityApi instanceof SremSPCapabilityApi) {
            return new SremCapability(SPCapabilityApi.getPublisherId(), SPCapabilityApi.getOriginatingCountry(), SPCapabilityApi.getProtocolVersion(), SPCapabilityApi.getQuadTree(), transformRedirectStatusApiToRedirectStatus(SPCapabilityApi.getRedirect()), ((SremSPCapabilityApi) SPCapabilityApi).getIds());
        }
        else if (SPCapabilityApi instanceof SsemSPCapabilityApi) {
            return new SsemCapability(SPCapabilityApi.getPublisherId(), SPCapabilityApi.getOriginatingCountry(), SPCapabilityApi.getProtocolVersion(), SPCapabilityApi.getQuadTree(), transformRedirectStatusApiToRedirectStatus(SPCapabilityApi.getRedirect()), ((SsemSPCapabilityApi) SPCapabilityApi).getIds());
        }
        else if (SPCapabilityApi instanceof CamSPCapabilityApi) {
            return new CamCapability(SPCapabilityApi.getPublisherId(), SPCapabilityApi.getOriginatingCountry(), SPCapabilityApi.getProtocolVersion(), SPCapabilityApi.getQuadTree(), transformRedirectStatusApiToRedirectStatus(SPCapabilityApi.getRedirect()), ((CamSPCapabilityApi) SPCapabilityApi).getStationTypes());
        }
        throw new RuntimeException("Subclass of CapabilityApi not possible to convert: " + SPCapabilityApi.getClass().getSimpleName());
    }

    private RedirectStatus transformRedirectStatusApiToRedirectStatus(SPRedirectStatusApi status) {
        if (status == null) {
            return RedirectStatus.OPTIONAL;
        }
        switch (status) {
            case MANDATORY:
                return RedirectStatus.MANDATORY;
            case NOT_AVAILABLE:
                return RedirectStatus.NOT_AVAILABLE;
            default:
                return RedirectStatus.OPTIONAL;
        }
    }
}
