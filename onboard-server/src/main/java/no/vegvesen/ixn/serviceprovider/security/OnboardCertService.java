package no.vegvesen.ixn.serviceprovider.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OnboardCertService {
    private final IdentityResolver identityResolver;
    private static final Logger logger = LoggerFactory.getLogger(OnboardCertService.class);

    public OnboardCertService(@Autowired JwtIdentityResolver identityResolver) {
        this.identityResolver = identityResolver;
    }

    public void checkIfCommonNameMatchesNameInApiObject(String apiName) {
        String commonName = identityResolver.getCallerName();

        if (!commonName.equals(apiName)) {
            logger.error("Received request for {}, but CN on certificate was {}. Rejecting...", apiName, commonName);
            String errorMessage = "Received request for %s, but CN on certificate was %s. Rejecting...";
            throw new CNAndApiObjectMismatchException(String.format(errorMessage, apiName, commonName));
        }
    }
}
