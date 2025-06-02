import {useQuery} from "@tanstack/react-query";
import {ServiceProviders} from "@/types/serviceProviders";

const fetchDeliveryEndpoints = async (commonName: string): Promise<Awaited<{
    serviceProviderName: string;
    matches: any[]
} | { serviceProviderName: string; matches: any[] } | {
    serviceProviderName: string;
    endpoints: Awaited<{ deliveryId: string; capabilityMatchApi: any[] } | {
        deliveryId: any;
        localDeliveryEndpointApi: any
    }>[]
}>[]> => {
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
                return {serviceProviderName: "unknown", matches: []};
            }

            const deliveriesResponse = await fetch(`/api/${commonName}/serviceproviders/${providerName}/deliveries`);

            if (!deliveriesResponse.ok) {
                console.error(`Couldn't fetch deliveries for ${providerName}`);
                return {serviceProviderName: providerName, matches: []};
            }

            const deliveryIds: string[] = await deliveriesResponse.json();

            const endpoints: Awaited<{ deliveryId: string; capabilityMatchApi: any[] } | {
                deliveryId: any;
                localDeliveryEndpointApi: any
            }>[] = await Promise.all(
                deliveryIds.map(async (deliveryId) => {
                    const response = await fetch(
                        `/api/${commonName}/serviceproviders/${providerName}/deliveries/${deliveryId}/endpoints`
                    );

                    if (!response.ok) {
                        console.error(`Couldn't fetch endpoints for delivery ${deliveryId} of ${providerName}`);
                        return {deliveryId, capabilityMatchApi: []};
                    }

                    const deliveryEndpoint = await response.json();
                    console.log(deliveryEndpoint)
                    return deliveryEndpoint
                })
            );
            return {
                serviceProviderName: providerName,
                endpoints
            };
        })
    );
};

const useFetchDeliveryEndpoints = (commonName: string, enabled: boolean) => {
    return useQuery({
        queryKey: ["deliveryEndpoints"],
        queryFn: () => fetchDeliveryEndpoints(commonName),
        enabled: enabled
    });
};

export { useFetchDeliveryEndpoints };