import {useQuery} from "@tanstack/react-query";
import {
    ServiceProviders,
} from "@/types/serviceProviders";

const fetchDeliveryIdsPerServiceProvider: (commonName: string) => Promise<Awaited<{
    serviceProviderName: string;
    deliveryIds: any
} | { serviceProviderName: string; deliveryIds: number }>[]> = async (commonName: string) => {
    const res = await fetch(`${commonName}/serviceproviders`);
    if (res.ok) {
        const serviceProviders: ServiceProviders[] = await res.json();
        const seasonedServiceProviders = await Promise.all (serviceProviders.map(async (serviceProvider) => {
            const fetchServiceProviderDeliveries = await fetch(
                `${commonName}/serviceproviders/${serviceProvider.name}/deliveries`
            );
            if (fetchServiceProviderDeliveries.ok) {
                const data = await fetchServiceProviderDeliveries.json();
                return { serviceProviderName: serviceProvider.name, deliveryIds: data };
            } else {
                console.error(
                    `error when fetching ${serviceProvider.name} - ${fetchServiceProviderDeliveries.status} - ${fetchServiceProviderDeliveries.statusText}`
                );
                return { serviceProviderName: serviceProvider.name, deliveryIds: 0 };
            }
        }));
        return Promise.all(seasonedServiceProviders);
    } else {
        const errorObj = await res.json();
        throw new Error(`${errorObj.status}: ${errorObj.error}`);
    }
};

const useFetchDeliveryIdsPerServiceProvider = (commonName: string) => {
    return useQuery({
        queryKey: ["deliveryIds"],
        queryFn: () => fetchDeliveryIdsPerServiceProvider(commonName),
    });
};

export { useFetchDeliveryIdsPerServiceProvider };