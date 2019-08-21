package no.vegvesen.ixn.federation.api.v1_0;

public final class RESTEndpointPaths {

	public static final String CAPABILITIES_PATH = "/capabilities";
	public static final String SUBSCRIPTION_PATH = "/subscription";
	public static final String SUBSCRIPTION_POLLING_PATH = "/{ixnName}/subscription/{subscriptionId}";

	public static final String SP_CAPS_PATH = "/capabilities/{serviceProviderName}";
	public static final String SP_SUBREQ_PATH = "/subscription/{serviceProviderName}";

}
