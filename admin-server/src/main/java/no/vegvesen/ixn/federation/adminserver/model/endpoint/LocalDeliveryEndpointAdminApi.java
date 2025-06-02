package no.vegvesen.ixn.federation.adminserver.model.endpoint;

import no.vegvesen.ixn.federation.adminserver.qpid.LocalDeliveryEndpointApi;

public record LocalDeliveryEndpointAdminApi(LocalDeliveryEndpointApi localDeliveryEndpointApi,boolean exists) {
}
