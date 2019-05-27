package no.vegvesen.ixn.federation.discoverer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

@Component
public class DiscovererResponseErrorHandler implements ResponseErrorHandler {

	private Logger logger = LoggerFactory.getLogger(DiscovererResponseErrorHandler.class);

	@Override
	public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
		return (clientHttpResponse.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR ||
				clientHttpResponse.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR);
	}


	// TODO: Decide how the client should handle various errors.
	@Override
	public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {

		if(clientHttpResponse.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR){
			logger.error("Response was in 500 series: {}", clientHttpResponse.getStatusCode());
		}else if(clientHttpResponse.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR){
			logger.error("Response was in 400 series: {}", clientHttpResponse.getStatusCode());
		}
	}
}
