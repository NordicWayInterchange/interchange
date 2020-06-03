package no.vegvesen.ixn.federation.repository;

/*-
 * #%L
 * neighbour-model
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
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NeighbourRepository extends CrudRepository<Neighbour, Integer> {

	Neighbour findByName(String name);

	@NonNull
	List<Neighbour> findAll();

	List<Neighbour> findByCapabilities_Status(Capabilities.CapabilitiesStatus capabilitiesStatus);
	List<Neighbour> findByCapabilities_StatusIn(Capabilities.CapabilitiesStatus... capabilitiesStatuses);

	List<Neighbour> findBySubscriptionRequest_Status(SubscriptionRequestStatus status);

	List<Neighbour> findByFedIn_StatusIn(SubscriptionRequestStatus... statuses);

	List<Neighbour> findNeighboursByFedIn_Subscription_SubscriptionStatusIn(SubscriptionStatus... subscriptionStatus);

	@Query(value = "select distinct i from Neighbour i join i.subscriptionRequest sr join sr.subscription s where sr.status = :subscriptionRequestStatus and s.subscriptionStatus = :subscriptionStatus")
	List<Neighbour> findInterchangesBySubscriptionRequest_Status_And_SubscriptionStatus(
			@Param("subscriptionRequestStatus") SubscriptionRequestStatus subscriptionRequestStatus,
			@Param("subscriptionStatus") SubscriptionStatus subscriptionStatus
	);

}
