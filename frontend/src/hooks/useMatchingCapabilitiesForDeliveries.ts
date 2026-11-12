import { useQuery } from "@tanstack/react-query";
import { ExtendedDelivery } from "@/types/delivery";

const fetchMatchingCapabilitiesForDeliveries: (
  commonName: string,
  selector: string
) => Promise<ExtendedDelivery[]> = async (commonName, selector) => {
  const params = new URLSearchParams();
  if (selector.trim()) {
    params.set("selector", selector);
  }
  const query = params.toString();
  const res = await fetch(
    `/api/${commonName}/deliveries/capabilities${query ? `?${query}` : ""}`
  );

  if (res.ok) {
    const capabilities: ExtendedDelivery[] = await res.json();
    return capabilities;
  } else {
    const errorObj = await res.json();
    if (errorObj?.errorCode && errorObj?.message) {
      throw new Error(`${errorObj.errorCode}: ${errorObj.message}`);
    }
    throw new Error(
      errorObj?.message ||
        (typeof errorObj === "string"
          ? errorObj
          : "Failed to fetch matching capabilities")
    );
  }
};

const useMatchingCapabilitiesForDeliveries = (commonName: string, selector: string) => {
  return useQuery({
    queryKey: ["matchingDeliveries", commonName, selector],
    queryFn: () => fetchMatchingCapabilitiesForDeliveries(commonName, selector),
    enabled: Boolean(commonName),
    retry: false,
  });
};

export { useMatchingCapabilitiesForDeliveries };
