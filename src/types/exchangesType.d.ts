
export class ExchangesType {
    id: string;
    name: string;
    durable: boolean;
    type: string;
    bindings: Array<Binding>;
}

export type Binding = {
    bindingKey: string;
    destination: string;
    arguments: object;
}
