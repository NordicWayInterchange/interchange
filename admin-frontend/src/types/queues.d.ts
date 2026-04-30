export type queues = {
    id: string;
    name: string;
    durable: string;
    maximumMessageTtl: number;
    ensureNondestructiveConsumers: boolean;
}