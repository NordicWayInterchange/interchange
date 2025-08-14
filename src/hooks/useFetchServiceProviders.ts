import {useQuery} from "@tanstack/react-query";
import {ServiceProviders} from "@/types/serviceProviders";

const fetchServiceProviders = async (
    commonName: string,
): Promise<any> => {
    const res = await fetch(`/api/${commonName}/serviceproviders`);
    if (res.ok) {
        const serviceProviders: ServiceProviders[] = await res.json();
        const seasonedServiceProviders = await Promise.all (serviceProviders.map(async (serviceProvider) => {
            let fetchServiceProviderPrivatechannels = null;
            let fetchServiceProviderPrivatechannelsPeer = null;
            let privateChannelsData = null;
            let peersData = null;
            try {
                fetchServiceProviderPrivatechannels = await fetch(
                   `/api/${commonName}/serviceproviders/${serviceProvider.name}/privatechannels`
               );
                if (fetchServiceProviderPrivatechannels.ok) {
                     privateChannelsData = await fetchServiceProviderPrivatechannels.json();
                }
           } catch (err) {
               console.error(
                   `error when fetching ${serviceProvider.name} - ${fetchServiceProviderPrivatechannels?.status} - ${fetchServiceProviderPrivatechannels?.statusText}`
               );
               return {...serviceProvider, privatechannels: 0 };
           }

           try {
               fetchServiceProviderPrivatechannelsPeer = await fetch(
                   `/api/${commonName}/serviceproviders/${serviceProvider.name}/privatechannels/peer`
               );
               if (fetchServiceProviderPrivatechannelsPeer.ok) {
                    peersData = await fetchServiceProviderPrivatechannelsPeer.json();
               }
           } catch (err) {
               console.error(
                   `error when fetching ${serviceProvider.name} - ${fetchServiceProviderPrivatechannelsPeer?.status} - ${fetchServiceProviderPrivatechannelsPeer?.statusText}`
               );
               return {...serviceProvider, privatechannelsPeer: 0 };
           }
            return {
                ...serviceProvider,
                privatechannels: privateChannelsData ?? [],
                privatechannelsPeer: peersData ?? [],
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