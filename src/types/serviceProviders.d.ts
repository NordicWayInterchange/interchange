import {Application, CapabilitiesStatus, Capability, Metadata} from "@/types/neighbours";
import {BiQueueEndpointResponse} from "@/types/BiQueueResponse";

export type ServiceProviders = {
    id: number;
    name: string;
    biQueueEndpoint: BiQueueEndpointResponse;
    subscriptions: Array<ServiceProviderSubscriptions>;
    capabilities: Array<ServiceProviderCapabilities>;
    deliveries: Array<ServiceProviderDeliveries>;
    privateChannels: Array<ServiceProviderPrivateChannels>;
    privateChannelsPeer: Array<ServiceProviderPrivateChannelsPeer>;
};

export type ServiceProviderSubscriptions = {
    id: number;
    status: SubscriptionStatus;
    selector: string;
    endpoints: Array<Endpoint>;
    lastUpdatedTimestamp: number;
    consumerCommonName: string;
    connections: Array<Connection>
    description: string;
    errorMessage: string;
};

export type ServiceProviderCapabilities = {
    id: number;
    application: Application;
    metadata: Metadata;
    status: CapabilitiesStatus;
    shards: Array<Shard>;
    createdTimestamp: number;
};

export type Shard = {
    shardId: number;
    exchangeName: string;
    selector: string;
}

export enum CapabilitiesStatus {
    UNKNOWN = "unknown",
    KNOWN = "known",
    FAILED = "failed"
}

export type ServiceProviderDeliveries = {
    id: string;
    selector: string;
    status: DeliveryStatus;
    endpoints: Array<Endpoint>;
    lastUpdatedTimestamp: number;
    description: string;
};

export enum SubscriptionStatus {
    REQUESTED = "requested",
    CREATED = "created",
    ILLEGAL = "illegal",
    NOT_VALID = "not_valid",
    NO_OVERLAP = "no_overlap",
    RESUBSCRIBE = "resubscribe",
    ERROR = "error"
}

export enum DeliveryStatus {
    REQUESTED = "requested",
    CREATED = "created",
    ILLEGAL = "illegal",
    NOT_VALID = "not_valid",
    NO_OVERLAP = "no_overlap",
}


export type Endpoint = {
    host: string;
    port: number;
    source: string;
    target: string;
    maxBandwidth: number;
    maxMessageRate: number;
};

export type Connection = {
    id: number;
    source: string;
    destination: string;
};

export type ServiceProviderPrivatechannels = {
    id: string;
    peers: Array<string>;
    status: PrivateChannelStatus;
    description: string;
    endpoint: EndPoint;
    lastUpdated: number;
};

export type ServiceProviderPrivateChannelsPeer = {
    id: string;
    peers: Array<string>;
    status: PrivateChannelStatus;
    description: string;
    endpoint: EndPoint;
    lastUpdated: number;
};

export enum PrivateChannelStatus {
    REQUESTED = "requested",
    CREATED = "created",
    TEAR_DOWN = "tear_down"
}

export type EndPoint = {
    host: string;
    port: number;
    queueName: string;
};

export type ExtendedServiceProviders = ServiceProviders & {
    privateChannels: ServiceProviderPrivatechannels;
};
