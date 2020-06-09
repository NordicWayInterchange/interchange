package no.vegvesen.ixn.federation.qpid;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix ="routing-configurer")
public class RoutingConfigurerProperties {

	/**
	 * how often the routing configurer will run, interval in milliseconds
	 */
	Integer interval = 10000;

	public Integer getInterval() {
		return interval;
	}

	public void setInterval(Integer interval) {
		this.interval = interval;
	}
}
