package no.vegvesen.ixn.federation.discoverer;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix ="graceful-backoff")
public class GracefulBackoffProperties {

	private int startIntervalLength;
	private int numberOfAttempts;
	private int randomShiftUpperLimit;

	private String checkInterval;
	private String checkOffset;

	public int getStartIntervalLength() {
		return startIntervalLength;
	}

	public void setStartIntervalLength(int startIntervalLength) {
		this.startIntervalLength = startIntervalLength;
	}

	public int getNumberOfAttempts() {
		return numberOfAttempts;
	}

	public void setNumberOfAttempts(int numberOfAttempts) {
		this.numberOfAttempts = numberOfAttempts;
	}

	public int getRandomShiftUpperLimit() {
		return randomShiftUpperLimit;
	}

	public void setRandomShiftUpperLimit(int randomShiftUpperLimit) {
		this.randomShiftUpperLimit = randomShiftUpperLimit;
	}

	public String getCheckInterval() {
		return checkInterval;
	}

	public void setCheckInterval(String checkInterval) {
		this.checkInterval = checkInterval;
	}

	public String getCheckOffset() {
		return checkOffset;
	}

	public void setCheckOffset(String checkOffset) {
		this.checkOffset = checkOffset;
	}

	@Override
	public String toString() {
		return "GracefulBackoffProperties{" +
				"startIntervalLength=" + startIntervalLength +
				", numberOfAttempts=" + numberOfAttempts +
				", randomShiftUpperLimit=" + randomShiftUpperLimit +
				", checkInterval=" + checkInterval +
				", checkOffset=" + checkOffset +
				'}';
	}
}
