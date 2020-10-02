package no.vegvesen.ixn.federation.discoverer;

import no.vegvesen.ixn.federation.model.Neighbour;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix ="graceful-backoff")
public class GracefulBackoffProperties {

	/**
	 * The length, in milliseconds, of the base waiting time of backoff.
	 */
	private int startIntervalLength = 2000;

	/**
	 * Number of times we are allowed to reattempt a POST or a GET to a failed neighbour.
	 */
	private int numberOfAttempts = 4;

	/**
	 * The upper limit, in milliseconds, of the random shift used in backoff POST and GET.
	 */
	private int randomShiftUpperLimit = 60000;

	/**
	 * Time, in milliseconds, between each lookup of failed neighbours in the database.
	 */
	private String checkInterval = "30000";

	/**
	 * Time, in milliseconds, from application startup until lookup for failed neighbours in the database.
	 */
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
