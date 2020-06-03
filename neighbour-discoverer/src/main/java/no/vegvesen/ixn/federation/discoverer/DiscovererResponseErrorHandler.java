package no.vegvesen.ixn.federation.discoverer;

/*-
 * #%L
 * neighbour-discoverer
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
