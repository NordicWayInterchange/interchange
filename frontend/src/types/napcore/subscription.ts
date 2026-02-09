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
  dlqName: string;
};

export type SubscriptionsSubscription = {
  id: string;
  selector: string;
  lastStatusChange: number;
  description: string;
  status: SubscriptionStatus;
  endpoints: Array<Endpoint>;
};

export type Subscriptions = {
  name: string;
  subscriptions: SubscriptionsSubscription[];
  version: string;
};

export type SubscriptionRequest = {
  selector: string;
};
