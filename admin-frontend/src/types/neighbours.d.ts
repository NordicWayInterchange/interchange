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


export type Neighbours = {
    neighbour_id: number;
    name: string;
    capabilities: Array<NeighbourCapabilities>;
    neighbourRequestedSubscriptions: neighbourRequestedSubscriptions;
    ourRequestedSubscriptions: OurRequestedSubscriptions;
    connectionStatus: ConnectionStatus;
    lastFailedConnectionAttempt: number;
    controlConnection: ControlConnection;
    lastUpdated: number;
    ignore: boolean;
};

export type NeighbourCapabilities = {
    id: number;
    status: CapabilitiesStatus;
    capabilities: Capability;
    lastUpdated: number;
    lastCapabilityExchange: number;
};

export type Capability = {
    id: number;
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
    id: number;
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
    subreq_id: number;
    id: string;
    subscriptionStatus: SubscriptionStatus;
    status: SubscriptionStatus;
    selector: string;
    path: string;
    consumerCommonName: string;
    endpoints: Array<Endpoint>;
    lastUpdatedTimestamp: number;
    description: string;
};

export type OurRequestedSubscriptions = {
    subreq_id: number;
    subscriptions: Array<Subscription>;
    successfulRequest: number;
};

export type ControlConnection = {
    id: number;
    backoffStart: number;
    backoffAttempts: number;
    connectionStatus: string;
    unreachableTime: number;
    lastFailedConnectionAttempt: number;
}
