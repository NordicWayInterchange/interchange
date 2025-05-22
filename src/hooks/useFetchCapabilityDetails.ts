import { useQuery } from "@tanstack/react-query";

const fetchCapabilityDetails = async (
    commonName: string,
    serviceProviderName: string,
    deliveryId: string,
    capabilityId: string
): Promise<any> => {
    const res = await fetch(
        `/api/${commonName}/serviceproviders/${serviceProviderName}/deliveries/${deliveryId}/matches/${capabilityId}`
    );
    if (res.ok) {
        return res.json();
    } else {
        const errorObj = await res.json();
        throw new Error(`${errorObj.errorCode}: ${errorObj.message}`);
    }
};

const useFetchCapabilityDetails = (
    commonName: string,
    serviceProviderName: string,
    deliveryId: string | null,
    capabilityId: string | null
) => {
    return useQuery({
        queryKey: ["capabilitiesDetails", commonName, serviceProviderName, deliveryId, capabilityId],
        queryFn: () =>
            fetchCapabilityDetails(commonName, serviceProviderName, deliveryId!, capabilityId!),
        enabled: !!commonName && !!serviceProviderName && !!deliveryId && !!capabilityId, // prevents firing query on nulls
    });
};


export { useFetchCapabilityDetails };
