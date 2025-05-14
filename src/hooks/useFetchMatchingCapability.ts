import { useQuery } from "@tanstack/react-query";
import { ServiceProviders } from "@/types/serviceProviders";

type MatchResult = {
    deliveryId: string;
    capabilityMatchApi: any[];
};

type ProviderMatches = {
    serviceProviderName: string;
    matches: MatchResult[];
};

const fetchMatchingCapability = async (commonName: string): Promise<ProviderMatches[]> => {
    const serviceProvidersResponse = await fetch(`/api/${commonName}/serviceproviders`);

    if (!serviceProvidersResponse.ok) {
        const error = await serviceProvidersResponse.json();
        throw new Error(`${error.status}: ${error.error}`);
    }

    const serviceProviders: ServiceProviders[] = await serviceProvidersResponse.json();

    const results = await Promise.all(
        serviceProviders.map(async (provider) => {
            const providerName = provider?.name;

            if (!providerName) {
                console.warn("Service provider name is missing", provider);
                return { serviceProviderName: "unknown", matches: [] };
            }

            const deliveriesResponse = await fetch(`/api/${commonName}/serviceproviders/${providerName}/deliveries`);

            if (!deliveriesResponse.ok) {
                console.error(`Couldn't fetch deliveries for ${providerName}`);
                return { serviceProviderName: providerName, matches: [] };
            }

            const deliveryIds: string[] = await deliveriesResponse.json();
            console.log('deliveryIds', deliveryIds);

            const matches: MatchResult[] = await Promise.all(
                deliveryIds.map(async (deliveryId) => {
                    const matchResponse = await fetch(
                        `/api/${commonName}/serviceproviders/${providerName}/deliveries/${deliveryId}/matches`
                    );

                    if (!matchResponse.ok) {
                        console.error(`Couldn't fetch matches for delivery ${deliveryId} of ${providerName}`);
                        return { deliveryId, capabilityMatchApi: [] };
                    }

                    const matchData = await matchResponse.json();

                    return {
                        deliveryId: matchData.deliveryId,
                        capabilityMatchApi: matchData.capabilityMatchApi ?? []
                    };
                })
            );
            return {
                serviceProviderName: providerName,
                matches
            };
        })
    );

    return results;
};

const useFetchMatchingCapability = (commonName: string) => {
    return useQuery({
        queryKey: ["matchingCapabilities"],
        queryFn: () => fetchMatchingCapability(commonName),
    });
};

export { useFetchMatchingCapability };