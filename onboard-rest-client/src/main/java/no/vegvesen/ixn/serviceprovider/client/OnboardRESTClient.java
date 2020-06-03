package no.vegvesen.ixn.serviceprovider.client;

/*-
 * #%L
 * onboard-rest-client
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

import no.vegvesen.ixn.federation.api.v1_0.DataTypeApi;
import no.vegvesen.ixn.serviceprovider.model.LocalDataType;
import no.vegvesen.ixn.serviceprovider.model.LocalDataTypeList;
import no.vegvesen.ixn.serviceprovider.model.LocalSubscriptionApi;
import no.vegvesen.ixn.serviceprovider.model.LocalSubscriptionListApi;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;

public class OnboardRESTClient {


    private RestTemplate restTemplate;
    private final String server;
    private final String user;

    public OnboardRESTClient(SSLContext sslContext, String server, String user) {
        this.restTemplate = new RestTemplate(
                new HttpComponentsClientHttpRequestFactory(
                        HttpClients
                                .custom()
                                .setSSLContext(sslContext)
                                .build()
                )
        );
        this.server = server;
        this.user = user;
    }

    public LocalDataType addCapability(DataTypeApi capability) {
        HttpHeaders headers =  new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<DataTypeApi> entity = new HttpEntity<>(capability,headers);
        return restTemplate.exchange(server + "/" + user + "/capabilities", HttpMethod.POST, entity, LocalDataType.class).getBody();
    }

    public LocalDataTypeList getServiceProviderCapabilities() {
        return restTemplate.getForEntity(server + "/" + user + "/capabilities", LocalDataTypeList.class).getBody();
    }


    public LocalSubscriptionListApi getServiceProviderSubscription() {
		String url = String.format("%s/%s/subscriptions/", server, user);
		return restTemplate.getForEntity(url, LocalSubscriptionListApi.class).getBody();
    }

    public void deleteSubscriptions(Integer localSubscriptionId) {
        restTemplate.delete(String.format("%s/%s/subscriptions/%s", server, user, localSubscriptionId));
    }

    public LocalSubscriptionApi addSubscription(DataTypeApi subscription) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<DataTypeApi> entity = new HttpEntity<>(subscription,headers);
		String url = String.format("/%s/subscriptions", user) ;
		return restTemplate.exchange(server + url, HttpMethod.POST, entity, LocalSubscriptionApi.class).getBody();
    }

    public void deleteCapability(Integer capabilityId) {
		restTemplate.delete(String.format("%s/%s/capabilities/%s", server, user, capabilityId));
    }
}


