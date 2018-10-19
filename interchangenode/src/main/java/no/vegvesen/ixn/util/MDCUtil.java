package no.vegvesen.ixn.util;

import no.vegvesen.ixn.model.DispatchMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.jms.JMSException;
import javax.jms.TextMessage;

public class MDCUtil {
	private static final String MSGGUID = "msgguid";
	private static final String FROM = "from";
	private static final Logger logger = LoggerFactory.getLogger(MDCUtil.class);

	public static void setLogVariables(TextMessage message) {
		try {
			MDC.put(MSGGUID, message.getJMSMessageID());
		} catch (JMSException e) {
			logger.error("Could not set log variables", e);
		}
	}

	public static void removeLogVariables() {
		MDC.remove(FROM);
		MDC.remove(MSGGUID);
		logger.trace("Removed MDC-variables");
	}
}
