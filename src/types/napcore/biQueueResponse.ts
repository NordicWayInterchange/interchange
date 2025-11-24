export type BiQueue = {
  name: string;
  access: boolean;
}

export type BiQueueResponse = {
  access: boolean;
}

export type BiQueueEndpointResponse = {
  brokerExternalName: string;
  messageChannelPort: number;
  queueName: string,
}