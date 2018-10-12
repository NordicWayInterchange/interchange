package no.vegvesen.ixn.util;

import no.vegvesen.ixn.model.DispatchMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class MDCUtil {
	private static final String MSGGUID = "msgguid";
	private static final String FROM = "from";
	private static final Logger logger = LoggerFactory.getLogger(MDCUtil.class);

	public static void setLogVariables(DispatchMessage message) {
		MDC.put(MSGGUID, message.getId());
	}

	public static void removeLogVariables() {
		MDC.remove(FROM);
		MDC.remove(MSGGUID);
		logger.trace("Removed MDC-variables");
	}
}
