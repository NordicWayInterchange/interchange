package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.MessageForwardUtil;
import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicBoolean;

public class MessageCollectorListener implements MessageListener, ExceptionListener {
	private AtomicBoolean running;
	private final Sink sink;
	private final Source source;
	private Logger log = LoggerFactory.getLogger(MessageCollectorListener.class);

	MessageCollectorListener(Sink sink, Source source) {
		this.sink = sink;
		this.source = source;
		this.running = new AtomicBoolean(true);
	}

	@Override
	public void onMessage(Message message) {
		log.debug("Message received!");
		if (running.get()) {
			try {
				Object timestamp = message.getObjectProperty("timestamp");
				if (timestamp != null && timestamp.getClass().getName().contains("proton.amqp.UnsignedInteger")) {
					log.warn("Ignoring message with illegal header values");
					return;
				}
				MessageForwardUtil.send(source.getProducer(), message);
			} catch (JMSException e) {
				log.error("Problem receiving message", e);
				logMessageHeadersAndBody(message);
				if (e.getMessage().contains("Application properties do not allow non-primitive values")){
					log.warn("Ignoring message with illegal header values");
				}
				else {
					teardown();
					throw new MessageCollectorException(e);
				}
			}
		} else {
			log.debug("Got message, but listener is not running");
			this.teardown();
			throw new MessageCollectorException("Not running!");
		}
	}

	private void logMessageHeadersAndBody(Message message) {
		try {
			log.debug("Message of type {}", message.getClass().getName());
			Enumeration propertyNames = message.getPropertyNames();
			while (propertyNames.hasMoreElements()) {
				String propertyName = (String) propertyNames.nextElement();
				try {
					Object property = message.getObjectProperty(propertyName);
					if (property instanceof String){
						log.debug("String property {} value: [{}]", propertyName, message.getStringProperty(propertyName));
					}
					else if (property instanceof Integer) {
						log.debug("Integer property {} value: [{}]", propertyName, message.getIntProperty(propertyName));
					}
					else if (property instanceof Long) {
						log.debug("Long property {} value: [{}]", propertyName, message.getLongProperty(propertyName));
					}
					else if (property instanceof Double) {
						log.debug("Double property {} value: [{}]", propertyName, message.getDoubleProperty(propertyName));
					}
					else {
						log.warn("Property {} of unknown type {}", propertyName, property.getClass().getName());
					}
				} catch (JMSException e) {
					log.error("Error getting property value for property name {}", propertyName, e);
				}
			}
			if (log.isTraceEnabled()) {
				log.trace("Message body [{}]", message.getBody(String.class));
			}
		} catch (JMSException e) {
			log.error("Error while getting property names of remote message", e);
		}
	}

	public void teardown() {
		try {
			sink.close();
		} catch (Exception ignore) {
		}
		try {
			source.close();
		} catch (Exception ignore) {
		} finally {
			running.set(false);
		}
	}

	@Override
	public void onException(JMSException e) {
		log.error("Exception caught", e);
		this.teardown();
	}


	boolean isRunning() {
		return running.get();
	}
}
