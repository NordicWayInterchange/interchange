export type BiQueue = {
    name: string;
    access: boolean;
}

export type BiQueueResponse = {
    access: boolean;
}

export type BiQueueEndpointsApi = {
    messageType: any;
    biqueueEndpointResponse: BiqueueEndpointResponse;
}

export type BiqueueEndpointResponse = {
    brokerExternalName: string;
    messageChannelPort: number;
    queueName: string,
}