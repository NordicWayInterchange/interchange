package no.vegvesen.ixn.federation.model;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix ="graceful-backoff")
public class GracefulBackoffProperties {

	private int startIntervalLength = 2000;
	private int numberOfAttempts = 4;
	private int randomShiftUpperLimit = 60000;

	private String checkInterval = "30000";
	private String checkOffset = "60000";

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
