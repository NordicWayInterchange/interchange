package no.vegvesen.ixn.federation.service;

/*-
 * #%L
 * neighbour-service
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

import no.vegvesen.ixn.federation.api.v1_0.SubscriptionStatus;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(initializers = {PostgresTestcontainerInitializer.Initializer.class})
public class NeighbourServiceIT {

	@MockBean
	RestTemplate restTemplate;

	@Autowired
	private NeighbourRepository repository;


    @Autowired
    private NeighbourService service;

	@Test
	public void serviceIsAutowired() {
		assertThat(service).isNotNull();
	}

	@Test
	public void testFetchingNeighbourWithCorrectStatus() {
		Neighbour interchangeA = new Neighbour("interchangeA",
				new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.emptySet()),
				new SubscriptionRequest(SubscriptionRequestStatus.ESTABLISHED, Collections.emptySet()),
				new SubscriptionRequest(SubscriptionRequestStatus.ESTABLISHED, Sets.newLinkedHashSet(new Subscription("originatingCountry = 'NO'", SubscriptionStatus.CREATED))));
		Neighbour interchangeB = new Neighbour("interchangeB",
				new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.emptySet()),
				new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Sets.newLinkedHashSet(new Subscription("originatingCountry = 'NO'", SubscriptionStatus.REQUESTED))),
				new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Sets.newLinkedHashSet(new Subscription("originatingCountry = 'NO'", SubscriptionStatus.REQUESTED))));
		repository.save(interchangeA);
		repository.save(interchangeB);

		List<Neighbour> interchanges = service.listNeighboursToConsumeMessagesFrom();
		assertThat(interchanges).size().isEqualTo(1);
	}


}
