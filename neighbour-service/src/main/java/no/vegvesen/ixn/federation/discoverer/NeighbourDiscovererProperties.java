package no.vegvesen.ixn.federation.discoverer;

/*-
 * #%L
 * neighbour-service
 * %%
 * Copyright (C) 2019 - 2020 Nordic Way 3
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix ="discoverer")
public class NeighbourDiscovererProperties {

	/**
	 * Time, in milliseconds, between each capability post to neighbours.
	 */
	private String capabilitiesUpdateInterval = "15000";

	/**
	 * Time, in milliseconds, from application start-up to first attempt at polling subscriptions."
	 */
	private String capabilityPostInitialDelay = "3000";

	/**
	 * Time, in milliseconds, between each attempt at posting a subscription request.
	 */
	private String subscriptionRequestUpdateInterval = "15000";

	/**
	 * Time, in milliseconds, from application start-up to subscription request.
	 */
	private String subscriptionRequestInitialDelay = "10000";

	/**
	 * Time, in milliseconds, between each attempt at polling a subscription.
	 */
	private String subscriptionPollUpdateInterval = "15000";

	/**
	 * Time, in milliseconds, from application start-up to first attempt at polling subscriptions.
	 */
	private String subscriptionPollInitialDelay = "20000";

	/**
	 * Time, in milliseconds, between each check for updated Service Providers.
	 */
	private String updatedServiceProviderCheckInterval = "20000";
	private String dnsLookupInterval = "15000";
	private String dnsInitialStartDelay = "5000";

	/**
	 * Number of times we are allowed to poll one subscription.
	 */
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
