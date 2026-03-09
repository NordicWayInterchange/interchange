import { useQuery } from "@tanstack/react-query";
import { ExtendedDelivery } from "@/types/delivery";
import { DeliveriesDelivery } from "@/types/napcore/delivery";

const fetchDeliveries: (
  commonName: string
) =>
  Promise<ExtendedDelivery[]> = async (commonName: string) => {
  const res = await fetch(
    `/api/${commonName}/deliveries`
  );

  if (res.ok) {
    const deliveries: DeliveriesDelivery[] = await res.json();
    const seasonedDeliveries = deliveries.map(async (sub) => {
      const fetchNumberOfCapabilities = await fetch(
        `/api/${commonName}/delivery-count/?selector=${sub.selector}`
      );
      if (fetchNumberOfCapabilities.ok) {
        const data = await fetchNumberOfCapabilities.json();
        return { ...sub, capabilityMatches: data };
      } else {
        console.error(
          `error when fetching ${sub.selector} - ${fetchNumberOfCapabilities.status} - ${fetchNumberOfCapabilities.statusText}`
        );
        return { ...sub, capabilityMatches: 0 };
      }
    });
    return Promise.all(seasonedDeliveries);
  } else {
    const errorObj = await res.json();
    throw new Error(`${errorObj.status}: ${errorObj.error}`);
  }
};

const useDeliveries = (commonName: string) => {
  return useQuery({
    queryKey: ["deliveries"],
    queryFn: () => fetchDeliveries(commonName),
  });
};

export { useDeliveries };

