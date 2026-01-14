export type BiQueue = {
  name: string;
  access: boolean;
}

export type BiQueueResponse = {
  access: boolean;
}

export type BiQueueEndpointResponse = {
  messageType: any;
  biqueueEndpoints: Array<biqueueEndpointsApi>;
}

export type biqueueEndpointsApi = {
  brokerExternalName: string;
  messageChannelPort: number;
  queueName: string,
}