import { useQuery } from "@tanstack/react-query";

const fetchFetchShardDetails = async (
    commonName: string,
    serviceProviderName: string,
    deliveryId: string,
    capabilityId: string,
    shardId: string
): Promise<any> => {
    const res = await fetch(
        `/api/${commonName}/serviceproviders/${serviceProviderName}/deliveries/${deliveryId}/matches/${capabilityId}/${shardId}`
    );
    if (res.ok) {
        return res.json();
    } else {
        const errorObj = await res.json();
        throw new Error(`${errorObj.errorCode}: ${errorObj.message}`);
    }
};

const useFetchShardDetails = (
    commonName: string,
    serviceProviderName: string,
    deliveryId: string | null,
    capabilityId: string | null,
    shardId: string | null
) => {
    return useQuery({
        queryKey: ["capabilitiesDetails", commonName, serviceProviderName, deliveryId, capabilityId, shardId],
        queryFn: () =>
            fetchFetchShardDetails(commonName, serviceProviderName, deliveryId!, capabilityId!, shardId!),
        enabled: !!commonName && !!serviceProviderName && !!deliveryId && !!capabilityId && !!shardId
    });
};


export { useFetchShardDetails };
