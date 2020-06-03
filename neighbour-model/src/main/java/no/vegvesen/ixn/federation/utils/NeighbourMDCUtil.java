package no.vegvesen.ixn.federation.utils;

/*-
 * #%L
 * neighbour-model
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class NeighbourMDCUtil {
	private static final String DISCOVERING_INTERCHANGE = "local_interchange";
	private static final String NEIGHBOUR = "remote_interchange";

	private static final Logger logger = LoggerFactory.getLogger(NeighbourMDCUtil.class);

	public static void setLogVariables(String discoveringInterchange, String neighbour) {
		try {
			MDC.put(DISCOVERING_INTERCHANGE, discoveringInterchange);
			MDC.put(NEIGHBOUR, neighbour);
		} catch (RuntimeException e) {
			logger.error("Could not set log variables", e);
		}
	}

	public static void removeLogVariables() {
		MDC.remove(DISCOVERING_INTERCHANGE);
		MDC.remove(NEIGHBOUR);
		logger.trace("Removed logging variables.");
	}
}
