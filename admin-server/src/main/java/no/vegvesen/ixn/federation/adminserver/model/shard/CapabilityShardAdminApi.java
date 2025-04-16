package no.vegvesen.ixn.federation.adminserver.model.shard;

import no.vegvesen.ixn.federation.adminserver.qpid.CapabilityShardIdApi;

public record CapabilityShardAdminApi(CapabilityShardIdApi capabilityShardIdApi, boolean exchangeNameExists) {
}
