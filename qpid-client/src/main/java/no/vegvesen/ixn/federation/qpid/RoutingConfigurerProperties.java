package no.vegvesen.ixn.federation.qpid;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix ="routing-configurer")
public class RoutingConfigurerProperties {

	/**
	 * Time, in milliseconds,the routing configurer will run.
	 * Is used in scheduling settings, and must be provided as an application property, no default value.
	 */
	Integer interval;

	/**
	 * base url for the QPID rest api starting with protocol. Example https://bouveta-fed.itsinterchange.eu
	 */
	private String baseUrl;
	/**
	 * virtual host name in qpid, same as interchange node dns name
	 */
	private String vhost;

	public Integer getInterval() {
		return interval;
	}

	public void setInterval(Integer interval) {
		this.interval = interval;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getVhost() {
		return vhost;
	}

	public void setVhost(String vhost) {
		this.vhost = vhost;
	}
}
