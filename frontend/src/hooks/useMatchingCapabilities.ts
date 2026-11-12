import { useQuery } from "@tanstack/react-query";
import { ExtendedCapability } from "@/types/capability";

const fetchMatchingCapabilities: (
  commonName: string,
  selector: string
) => Promise<ExtendedCapability[]> = async (commonName, selector) => {
  const params = new URLSearchParams();
  if (selector.trim()) {
    params.set("selector", selector);
  }
  const query = params.toString();
  const res = await fetch(
    `/api/${commonName}/network/capabilities${query ? `?${query}` : ""}`
  );

  if (res.ok) {
    const capabilities: ExtendedCapability[] = await res.json();
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

const useMatchingCapabilities = (commonName: string, selector: string) => {
  return useQuery({
    queryKey: ["matchingCapabilities", commonName, selector],
    queryFn: () => fetchMatchingCapabilities(commonName, selector),
    enabled: Boolean(commonName),
    retry: false,
  });
};

export { useMatchingCapabilities };
