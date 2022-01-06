package no.vegvesen.ixn.onboard;


import no.vegvesen.ixn.federation.exceptions.DiscoveryException;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@ConfigurationPropertiesScan
public class SelfService {
	public static final String DEFAULT_MESSAGE_CHANNEL_PORT = "5671";
	private static Logger logger = LoggerFactory.getLogger(SelfService.class);
	ServiceProviderRepository repository;
	InterchangeNodeProperties interchangeNodeProperties;


	@Autowired
	public SelfService(InterchangeNodeProperties interchangeNodeProperties, ServiceProviderRepository repository) {
		this.interchangeNodeProperties = interchangeNodeProperties;
		this.repository = repository;
	}

	public List<ServiceProvider> getServiceProviders() {
		return repository.findAll();

	}

	//TODO: decide if centralize to the Properties class, or the service
	public String getNodeProviderName() {
		return interchangeNodeProperties.getName();
	}

	public String getMessageChannelUrl() {
		return getMessageChannelUrl(interchangeNodeProperties.getName(),interchangeNodeProperties.getMessageChannelPort());
	}

	public static String getMessageChannelUrl(String brokerName, String port) {
		try {
			if (port == null || port.equals(DEFAULT_MESSAGE_CHANNEL_PORT)) {
				return String.format("amqps://%s/", brokerName);
			} else {
				return String.format("amqps://%s:%s/",brokerName, port);
			}
		} catch (NumberFormatException e) {
			logger.error("Could not create message channel url for interchange {}", brokerName, e);
			throw new DiscoveryException(e);
		}
	}
}
