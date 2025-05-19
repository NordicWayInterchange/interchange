import {useQuery} from "@tanstack/react-query";
import {ServiceProviders} from "@/types/serviceProviders";

const fetchCapabilitiesDetails = async (commonName: string): Promise<Awaited<{ serviceProviderName: string; matches: any[] } | {
    matches: Awaited<{ deliveryId: string; capabilityMatchApi: any[] } | { capabilityDetails: Awaited<unknown>[] }>[]
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

            const matches: Awaited<{ deliveryId: string; capabilityMatchApi: any[] } | {
                capabilityDetails: Awaited<unknown>[]
            }>[] = await Promise.all(
                deliveryIds.map(async (deliveryId) => {
                    const matchResponse = await fetch(
                        `/api/${commonName}/serviceproviders/${providerName}/deliveries/${deliveryId}/matches`
                    );

                    if (!matchResponse.ok) {
                        console.error(`Couldn't fetch matches for delivery ${deliveryId} of ${providerName}`);
                        return {deliveryId, capabilityMatchApi: []};
                    }

                    const matchData = await matchResponse.json();
                    const matchList = matchData.capabilityMatchApi ?? [];

                    const capabilityDetails = await Promise.all(
                        matchList.map(async (match: { capabilityId: string }) => {
                            if (!match?.capabilityId) return null;
                            const detailResp = await fetch(
                                `/api/${commonName}/serviceproviders/${providerName}/deliveries/${deliveryId}/matches/${match.capabilityId}`
                            );
                            if (!detailResp.ok) {
                                console.error(`Couldn't fetch match detail for match ${match.capabilityId}`);
                                return null;
                            }
                            return await detailResp.json();
                        })
                    );

                    return {
                        capabilityDetails: capabilityDetails.find(Boolean) || null
                    };

                })
            );

            return {
                matches
            };
        })
    );
};

const useFetchCapabilitiesDetailsDetails = (commonName: string) => {
    return useQuery({
        queryKey: ["capabilitiesDetails"],
        queryFn: () => fetchCapabilitiesDetails(commonName),
    });
};

export { useFetchCapabilitiesDetailsDetails };