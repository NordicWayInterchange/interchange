package no.vegvesen.ixn.federation.discoverer;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix ="discoverer")
public class NeighbourDiscovererProperties {

	private String capabilitiesUpdateInterval = "15000";
	private String capabilityPostInitialDelay = "3000";
	private String subscriptionRequestUpdateInterval = "15000";
	private String subscriptionRequestInitialDelay = "10000";
	private String subscriptionPollUpdateInterval = "15000";
	private String subscriptionPollInitialDelay = "20000";
	private String updatedServiceProviderCheckInterval = "20000";
	private String dnsLookupInterval = "15000";
	private String dnsInitialStartDelay = "5000";
	private int subscriptionPollingNumberOfAttempts = 7;


	public String getDnsLookupInterval() {
		return dnsLookupInterval;
	}

	public void setDnsLookupInterval(String dnsLookupInterval) {
		this.dnsLookupInterval = dnsLookupInterval;
	}

	public String getDnsInitialStartDelay() {
		return dnsInitialStartDelay;
	}

	public void setDnsInitialStartDelay(String dnsInitialStartDelay) {
		this.dnsInitialStartDelay = dnsInitialStartDelay;
	}

	public String getCapabilitiesUpdateInterval() {
		return capabilitiesUpdateInterval;
	}

	public void setCapabilitiesUpdateInterval(String capabilitiesUpdateInterval) {
		this.capabilitiesUpdateInterval = capabilitiesUpdateInterval;
	}

	public String getCapabilityPostInitialDelay() {
		return capabilityPostInitialDelay;
	}

	public void setCapabilityPostInitialDelay(String capabilityPostInitialDelay) {
		this.capabilityPostInitialDelay = capabilityPostInitialDelay;
	}

	public String getSubscriptionRequestUpdateInterval() {
		return subscriptionRequestUpdateInterval;
	}

	public void setSubscriptionRequestUpdateInterval(String subscriptionRequestUpdateInterval) {
		this.subscriptionRequestUpdateInterval = subscriptionRequestUpdateInterval;
	}

	public String getSubscriptionRequestInitialDelay() {
		return subscriptionRequestInitialDelay;
	}

	public void setSubscriptionRequestInitialDelay(String subscriptionRequestInitialDelay) {
		this.subscriptionRequestInitialDelay = subscriptionRequestInitialDelay;
	}

	public String getSubscriptionPollUpdateInterval() {
		return subscriptionPollUpdateInterval;
	}

	public void setSubscriptionPollUpdateInterval(String subscriptionPollUpdateInterval) {
		this.subscriptionPollUpdateInterval = subscriptionPollUpdateInterval;
	}

	public int getSubscriptionPollingNumberOfAttempts() {
		return subscriptionPollingNumberOfAttempts;
	}

	public void setSubscriptionPollingNumberOfAttempts(int subscriptionPollingNumberOfAttempts) {
		this.subscriptionPollingNumberOfAttempts = subscriptionPollingNumberOfAttempts;
	}

	public String getSubscriptionPollInitialDelay() {
		return subscriptionPollInitialDelay;
	}

	public void setSubscriptionPollInitialDelay(String subscriptionPollInitialDelay) {
		this.subscriptionPollInitialDelay = subscriptionPollInitialDelay;
	}

	public String getUpdatedServiceProviderCheckInterval() {
		return updatedServiceProviderCheckInterval;
	}

	public void setUpdatedServiceProviderCheckInterval(String updatedServiceProviderCheckInterval) {
		this.updatedServiceProviderCheckInterval = updatedServiceProviderCheckInterval;
	}

	@Override
	public String toString() {
		return "NeighbourDiscovererProperties{" +
				"capabilitiesUpdateInterval='" + capabilitiesUpdateInterval + '\'' +
				", capabilityPostInitialDelay='" + capabilityPostInitialDelay + '\'' +
				", subscriptionRequestUpdateInterval='" + subscriptionRequestUpdateInterval + '\'' +
				", subscriptionRequestInitialDelay='" + subscriptionRequestInitialDelay + '\'' +
				", subscriptionPollUpdateInterval='" + subscriptionPollUpdateInterval + '\'' +
				", subscriptionPollInitialDelay='" + subscriptionPollInitialDelay + '\'' +
				", updatedServiceProviderCheckInterval='" + updatedServiceProviderCheckInterval + '\'' +
				", dnsLookupInterval='" + dnsLookupInterval + '\'' +
				", dnsInitialStartDelay='" + dnsInitialStartDelay + '\'' +
				", subscriptionPollingNumberOfAttempts=" + subscriptionPollingNumberOfAttempts +
				'}';
	}
}
