import {useQuery} from "@tanstack/react-query";
import {
    ServiceProviderCapabilities,
    ServiceProviderDeliveries,
    ServiceProviderPrivateChannels,
    ServiceProviderPrivateChannelsPeer,
    ServiceProviders,
    ServiceProviderSubscriptions
} from "@/types/serviceProviders";

// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-expect-error
const fetchServiceProviders: (commonName: string) => Promise<Awaited<{
    id: number;
    name: string;
    subscriptions: Array<ServiceProviderSubscriptions>;
    capabilities: Array<ServiceProviderCapabilities>;
    deliveries: Array<ServiceProviderDeliveries>;
    privateChannels: Array<ServiceProviderPrivateChannels>;
    privateChannelsPeer: Array<ServiceProviderPrivateChannelsPeer>;
    privatechannels: any
} | {
    id: number;
    name: string;
    subscriptions: Array<ServiceProviderSubscriptions>;
    capabilities: Array<ServiceProviderCapabilities>;
    deliveries: Array<ServiceProviderDeliveries>;
    privateChannels: Array<ServiceProviderPrivateChannels>;
    privateChannelsPeer: Array<ServiceProviderPrivateChannelsPeer>;
    privatechannels: number
}>[]> = async (commonName: string) => {
    const res = await fetch(`/api/${commonName}/serviceproviders`);
    if (res.ok) {
        const serviceProviders: ServiceProviders[] = await res.json();
        const seasonedServiceProviders = await Promise.all (serviceProviders.map(async (serviceProvider) => {
            let fetchServiceProviderPrivateChannels = null;
            let fetchServiceProviderPrivateChannelsPeer = null;
            let privateChannelsData = null;
            let peersData = null;
            try {
                fetchServiceProviderPrivateChannels = await fetch(
                   `/api/${commonName}/serviceproviders/${serviceProvider.name}/privatechannels`
               );
                if (fetchServiceProviderPrivateChannels.ok) {
                     privateChannelsData = await fetchServiceProviderPrivateChannels.json();
                }
           } catch (err) {
               console.error(
                   `error when fetching ${serviceProvider.name} - ${fetchServiceProviderPrivateChannels?.status} - ${fetchServiceProviderPrivateChannels?.statusText}`
               );
               return {...serviceProvider, privatechannels: 0 };
           }

           try {
               fetchServiceProviderPrivateChannelsPeer = await fetch(
                   `/api/${commonName}/serviceproviders/${serviceProvider.name}/privatechannels/peer`
               );
               if (fetchServiceProviderPrivateChannelsPeer.ok) {
                    peersData = await fetchServiceProviderPrivateChannelsPeer.json();
               }
           } catch (err) {
               console.error(
                   `error when fetching ${serviceProvider.name} - ${fetchServiceProviderPrivateChannelsPeer?.status} - ${fetchServiceProviderPrivateChannelsPeer?.statusText}`
               );
               return {...serviceProvider, privatechannelsPeer: 0 };
           }
            return {
                ...serviceProvider,
                privateChannels: privateChannelsData ?? [],
                privateChannelsPeer: peersData ?? [],
            };
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