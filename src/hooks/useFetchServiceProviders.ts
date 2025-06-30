import {useQuery} from "@tanstack/react-query";
import {
    ServiceProviderCapabilities, ServiceProviderDeliveries, ServiceProviderPrivateChannels,
    ServiceProviders,
    ServiceProviderSubscriptions
} from "@/types/serviceProviders";

const fetchServiceProviders: (commonName: string) => Promise<Awaited<{
    id: number;
    name: string;
    subscriptions: Array<ServiceProviderSubscriptions>;
    capabilities: Array<ServiceProviderCapabilities>;
    deliveries: Array<ServiceProviderDeliveries>;
    privateChannels: Array<ServiceProviderPrivateChannels>;
    privatechannels: any
} | {
    id: number;
    name: string;
    subscriptions: Array<ServiceProviderSubscriptions>;
    capabilities: Array<ServiceProviderCapabilities>;
    deliveries: Array<ServiceProviderDeliveries>;
    privateChannels: Array<ServiceProviderPrivateChannels>;
    privatechannels: number
}>[]> = async (commonName: string) => {
    const res = await fetch(`/api/${commonName}/serviceproviders`);
    if (res.ok) {
        const serviceProviders: ServiceProviders[] = await res.json();
        const seasonedServiceProviders = await Promise.all (serviceProviders.map(async (serviceProvider) => {
            const fetchServiceProviderPrivateChannels = await fetch(
                `/api/${commonName}/serviceproviders/${serviceProvider.name}/privatechannels`
            );
            if (fetchServiceProviderPrivateChannels.ok) {
                const data = await fetchServiceProviderPrivateChannels.json();
                return { ...serviceProvider, privatechannels: data };
            } else {
                console.error(
                    `error when fetching ${serviceProvider.name} - ${fetchServiceProviderPrivateChannels.status} - ${fetchServiceProviderPrivateChannels.statusText}`
                );
                return {...serviceProvider, privatechannels: 0 };
            }
        }));
        return Promise.all(seasonedServiceProviders);
    } else {
        const errorObj = await res.json();
        throw new Error(`${errorObj.status}: ${errorObj.error}`);
    }
};

const useFetchServiceProviders = (commonName: string) => {
    return useQuery({
        queryKey: ["serviceproviders"],
        queryFn: () => fetchServiceProviders(commonName),
    });
};

export { useFetchServiceProviders };