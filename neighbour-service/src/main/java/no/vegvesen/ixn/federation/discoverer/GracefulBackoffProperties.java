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

	public boolean canBeContacted(Neighbour neighbour) {
		return neighbour.canBeContacted(this.randomShiftUpperLimit, this.startIntervalLength);
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
