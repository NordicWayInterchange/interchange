import {CapabilitiesStatus, Capability} from "@/types/neighbours";

export type ServiceProviders = {
    name: string;
    subscriptions: Array<ServiceProviderSubscriptions>;
};

export type ServiceProviderSubscriptions = {
    id: string;
    status: SubscriptionStatus;
    selector: string;
    endpoints: Array<Endpoint>;
    lastUpdatedTimestamp: number;
    consumerCommonName: string;
    connections: Array<Connection>
    description: string;
    errorMessage: string;
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