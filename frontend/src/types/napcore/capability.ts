export enum RedirectStatus {
  OPTIONAL = "optional",
  MANDATORY = "mandatory",
  NOT_AVAILABLE = "not_available",
}

export type Application = {
  messageType: string;
  publisherId: string;
  publisherName: string;
  publicationType: string;
  protocolVersion: string;
  publicationId: string;
  originatingCountry: string;
  quadTree: Array<string>;
  causeCode?: Array<number>;
  shardCount: number;
  redirectPolicy: string;
};

export type Metadata = {
  shardCount: number;
  infoUrl: string;
  redirectPolicy: RedirectStatus;
  maxBandwidth: number;
  maxMessageRate: number;
  repetitionInterval: number;
};

export type Capability = {
  id: string;
  application: Application;
  metadata: Metadata;
};

export type CapabilityRequest = {
  Application: Application;
  Metadata: Metadata;
};

export type Publicationids = {
  id: string;
}
