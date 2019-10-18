package no.vegvesen.ixn.federation.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class MDCUtil {
	private static final String DISCOVERING_INTERCHANGE = "local_interchange";
	private static final String NEIGHBOUR = "remote_interchange";

	private static final Logger logger = LoggerFactory.getLogger(MDCUtil.class);

	public static void setLogVariables(String discoveringInterchange, String neighbour) {
		try {
			MDC.put(DISCOVERING_INTERCHANGE, discoveringInterchange);
			MDC.put(NEIGHBOUR, neighbour);
		} catch (RuntimeException e) {
			logger.error("Could not set log variables", e);
		}
	}

	public static void setDiscoveringInterchangeVariable(String discoveringInterchange){
		try {
			MDC.put(DISCOVERING_INTERCHANGE, discoveringInterchange);
		} catch (RuntimeException e) {
			logger.error("Could not set log variables", e);
		}
	}

	public static void setNeigbourVariable(String neighbour) {
		try {
			MDC.put(NEIGHBOUR, neighbour);
		} catch (RuntimeException e) {
			logger.error("Could not set log variables", e);
		}
	}

	public static void removeNeighbourVariable() {
		MDC.remove(NEIGHBOUR);
	}

	public static void removeLogVariables() {
		MDC.remove(DISCOVERING_INTERCHANGE);
		MDC.remove(NEIGHBOUR);
		logger.trace("Removed logging variables.");
	}
}
