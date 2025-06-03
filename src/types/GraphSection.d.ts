import {Capability} from "@/types/neighbours";

export type CopyTarget = {
    id: string;
    fullId: string;
    x: number;
    y: number;
};

export type GraphSectionProps = {
    serviceProviderName: string;
    matches: Match[];
};

export type Binding = {
    [key: string]: any;
    bindingKey: string;
    destination: string;

};

export type Shard = {
    capabilityShard: CapabilityShard;
    exchangeNameExists: boolean;
}

export type CapabilityShard = {
    shardId: string;
    exchangeName: string;
    selector: string;
};