package no.vegvesen.ixn.federation.adminserver.model.endpoint;

import no.vegvesen.ixn.federation.adminserver.model.serviceProvider.LocalSubscriptionEndpointApi;

public record LocalSubscriptionEndpointAdminApi(LocalSubscriptionEndpointApi localSubscriptionEndpointApi, boolean exists) {
}
