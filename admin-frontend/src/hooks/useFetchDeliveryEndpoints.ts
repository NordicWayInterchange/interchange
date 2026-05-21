import {useQuery} from "@tanstack/react-query";
import {ServiceProviders} from "@/types/serviceProviders";

const fetchDeliveryEndpoints = async (commonName: string, deliveryId: string): Promise<Awaited<{
    serviceProviderName: string;
    matches: any[]
} | { serviceProviderName: string; endpoints: any[] } | string[]>[]> => {
    const serviceProvidersResponse = await fetch(`/api/${commonName}/serviceproviders`);

    if (!serviceProvidersResponse.ok) {
        const error = await serviceProvidersResponse.json();
        throw new Error(`${error.status}: ${error.error}`);
    }

    const serviceProviders: ServiceProviders[] = await serviceProvidersResponse.json();

    return await Promise.all(
        serviceProviders.map(async (provider) => {
            const providerName = provider?.name;

            if (!providerName) {
                console.warn("Service provider name is missing", provider);
                return {serviceProviderName: "unknown", endpoints: []};
            }

            const deliveriesResponse = await fetch(`/api/${commonName}/serviceproviders/${providerName}/deliveries/${deliveryId}/endpoints`);

            if (!deliveriesResponse.ok) {
                console.error(`Couldn't fetch deliveries for ${providerName}`);
                return {serviceProviderName: providerName, endpoints: []};
            }

            const deliveryEndpoints: string[] = await deliveriesResponse.json();
            return deliveryEndpoints;
        })
    );
};

const useFetchDeliveryEndpoints = (commonName: string, deliveryId: string, enabled: boolean) => {
    return useQuery({
        queryKey: ["deliveryEndpoints"],
        queryFn: () => fetchDeliveryEndpoints(commonName, deliveryId),
        enabled: enabled
    });
};

export { useFetchDeliveryEndpoints };