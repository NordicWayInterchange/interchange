package no.vegvesen.ixn.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.jms.JMSException;
import javax.jms.Message;

public class MDCUtil {
	private static final String MSGGUID = "msgguid";

	private static final Logger logger = LoggerFactory.getLogger(MDCUtil.class);

	public static void setLogVariables(Message message) {
		try {
			MDC.put(MSGGUID, message.getJMSMessageID());
		} catch (JMSException e) {
			logger.error("Could not set log variables", e);
		}
	}

	public static void removeLogVariables() {
		MDC.remove(MSGGUID);
		logger.trace("Removed MDC-variables");
	}
}
