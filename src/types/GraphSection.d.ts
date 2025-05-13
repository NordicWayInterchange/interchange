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