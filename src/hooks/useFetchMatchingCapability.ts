import {useQuery} from "@tanstack/react-query";
import {
    ServiceProviders,
} from "@/types/serviceProviders";

const fetchMatchingCapability: (commonName: string) => Promise<Awaited<{
    serviceProviderName: string;
    matches: Awaited<any>[]
} | { serviceProviderName: string; matches: any[] }>[]> = async (commonName: string) => {
    const res = await fetch(`/api/${commonName}/serviceproviders`);
    if (res.ok) {
        const serviceProviders: ServiceProviders[] = await res.json();
        const seasonedServiceProviders = await Promise.all (serviceProviders.map(async (serviceProvider) => {
            const fetchServiceProviderDeliveries = await fetch(
                `${commonName}/serviceproviders/${serviceProvider.name}/deliveries`
            );
            if (fetchServiceProviderDeliveries.ok) {
                const deliveryIds: { id: string}[] = await fetchServiceProviderDeliveries.json();
                const matches = await Promise.all(
                    deliveryIds.map(async (deliveryId) => {
                        const matchRes = await fetch(`${commonName}/serviceproviders/${serviceProvider.name}/deliveries/${deliveryId}/matches`);
                        if (matchRes.ok) {
                            return await matchRes.json();
                        } else {
                            return { deliveryId: deliveryId, matchData: null };
                        }
                    })
                );
                return {
                    serviceProviderName: serviceProvider.name,
                    matches
                };
            } else {
                console.error(
                    `error when fetching ${serviceProvider.name} - ${fetchServiceProviderDeliveries.status} - ${fetchServiceProviderDeliveries.statusText}`
                );
                return {
                    serviceProviderName: serviceProvider.name,
                    matches: []
                };
            }
        }));
        return Promise.all(seasonedServiceProviders);
    } else {
        const errorObj = await res.json();
        throw new Error(`${errorObj.status}: ${errorObj.error}`);
    }
};

const useFetchMatchingCapability = (commonName: string) => {
    return useQuery({
        queryKey: ["matchingCapabilities"],
        queryFn: () => fetchMatchingCapability(commonName),
    });
};

export { useFetchMatchingCapability };