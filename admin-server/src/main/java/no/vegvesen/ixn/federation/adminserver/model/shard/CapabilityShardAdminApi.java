package no.vegvesen.ixn.federation.adminserver.model.shard;

import no.vegvesen.ixn.federation.adminserver.qpid.CapabilityShardApi;

public record CapabilityShardAdminApi(CapabilityShardApi capabilityShardIdApi, boolean exchangeNameExists) {
}
