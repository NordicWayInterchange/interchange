export type BiQueueEndpointResponse = {
    brokerExternalName: string;
    messageChannelPort: number;
    queueName: string,
}

export type BiQueueResponse = {
    access: boolean;
}