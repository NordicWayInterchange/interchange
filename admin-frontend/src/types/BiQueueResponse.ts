export type BiQueue = {
    name: string;
    access: boolean;
}

export type BiQueueResponse = {
    messageType: any;
    biqueueEndpointsApi: BiqueueEndpointsApi;
}

export type BiqueueEndpointsApi = {
    brokerExternalName: string;
    messageChannelPort: number;
    queueName: string,
}