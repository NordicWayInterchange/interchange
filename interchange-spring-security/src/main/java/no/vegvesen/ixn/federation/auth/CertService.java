package no.vegvesen.ixn.federation.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CertService{

	private static Logger logger = LoggerFactory.getLogger(CertService.class);

	public void checkIfCommonNameMatchesNameInApiObject(String apiName) {

		Authentication principal = SecurityContextHolder.getContext().getAuthentication();
		String commonName = principal.getName();

		if (!commonName.equals(apiName)) {
			logger.error("Received request for {}, but CN on certificate was {}. Rejecting...", apiName, commonName);
			String errorMessage = "Received request for %s, but CN on certificate was %s. Rejecting...";
			throw new CNAndApiObjectMismatchException(String.format(errorMessage, apiName, commonName));
		}
	}
}

