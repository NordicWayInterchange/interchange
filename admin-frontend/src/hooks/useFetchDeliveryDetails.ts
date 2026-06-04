import { useQuery } from "@tanstack/react-query";
import { Delivery } from "@/types/GraphSection";

export const fetchDeliveryDetails = async (
    commonName: string,
    serviceProviderName: string,
    deliveryId: string
): Promise<Delivery> => {
    const res = await fetch(
        `/api/${commonName}/serviceproviders/${serviceProviderName}/deliveries/${deliveryId}`
    );    if (!res.ok) {
        const errorObj = await res.json();
        throw new Error(`${errorObj.errorCode}: ${errorObj.message}`);
    }
    return res.json();
};

export const useFetchDeliveryDetails = (
    commonName: string | undefined,
    serviceProviderName: string | undefined,
    deliveryId: string | null,
    enabled: boolean
) => {
    return useQuery({
        queryKey: ["deliveryDetails", commonName, serviceProviderName, deliveryId],
        queryFn: () =>
            fetchDeliveryDetails(commonName!, serviceProviderName!, deliveryId!),
        enabled,
    });
};
