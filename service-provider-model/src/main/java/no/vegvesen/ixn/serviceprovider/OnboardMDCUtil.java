package no.vegvesen.ixn.serviceprovider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class OnboardMDCUtil {
	private static final String LOCAL_INTERCHANGE = "local_interchange";
	private static final String SERVICE_PROVIDER = "service_provider";

	private static final Logger logger = LoggerFactory.getLogger(OnboardMDCUtil.class);

	public static void setLogVariables(String interchangeNodeName, String serviceProviderName) {
		try {
			MDC.put(LOCAL_INTERCHANGE, interchangeNodeName);
			MDC.put(SERVICE_PROVIDER, serviceProviderName);
		} catch (RuntimeException e) {
			logger.error("Could not set log variables", e);
		}
	}

	public static void removeLogVariables() {
		MDC.remove(LOCAL_INTERCHANGE);
		MDC.remove(SERVICE_PROVIDER);
		logger.trace("Removed logging variables.");
	}
}
