export enum RedirectStatus {
    OPTIONAL = "optional",
    MANDATORY = "mandatory",
    NOT_AVAILABLE = "not_available",
}

export enum ConnectionStatus {
    CONNECTED = "connected",
    FAILED = "failed",
    UNREACHABLE = "unreachable"
}
export enum CapabilitiesStatus {
    UNKNOWN = "unknown",
    KNOWN = "known",
    FAILED = "failed"
}

export enum SubscriptionStatus {
    REQUESTED = "requested",
    CREATED = "created",
    ILLEGAL = "illegal",
    NOT_VALID = "not_valid",
    NO_OVERLAP = "no_overlap",
    RESUBSCRIBE = "resubscribe",
    ERROR = "error"
}


export type neighbours = {
    neighbour_id: number;
    name: string;
    capabilities: Array<NeighbourCapabilities>;
    neighbourRequestedSubscriptions: neighbourRequestedSubscriptions;
    ourRequestedSubscriptions: ourRequestedSubscriptions;
    connectionStatus: ConnectionStatus;
    lastFailedConnectionAttempt: number;
    lastUpdated: number;
    ignore: boolean;
};

export type NeighbourCapabilities = {
    id: string;
    status: CapabilitiesStatus;
    capabilities: Capability;
    lastUpdated: number;
    lastCapabilityExchange: number;
};

export type Capability = {
    id: string;
    application: Application;
    metadata: Metadata;
    createdTimestamp: number;
};

export type Application = {
    messageType: string;
    publisherId: string;
    publicationId: string;
    publisherName: string;
    publicationType: string;
    originatingCountry: string;
    protocolVersion: string;
    quadTree: Array<string>;
    causeCode?: Array<number>;
};

export type Metadata = {
    shardCount: number;
    infoUrl: string;
    redirectPolicy: RedirectStatus;
    maxBandwidth: number;
    maxMessageRate: number;
    repetitionInterval: number;
};

export type neighbourRequestedSubscriptions = {
    id: string;
    subscriptions: Array<Subscription>;
    successfulRequest: number;
}

export type Endpoint = {
    host: string;
    port: number;
    source: string;
    target: string;
    maxBandwidth: number;
    maxMessageRate: number;
};


export type Subscription = {
    subreq_id: string;
    subscriptionStatus: SubscriptionStatus;
    selector: string;
    path: string;
    consumerCommonName: string;
    endpoints: Array<Endpoint>;
    lastUpdatedTimestamp: number;
    description: string;
};

export type ourRequestedSubscriptions = {
    subreq_id: string;
    subscriptions: Array<Subscription>;
    successfulRequest: number;
};
