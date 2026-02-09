import { useQuery } from "@tanstack/react-query";
import { ExtendedCapability } from "@/types/capability";

const fetchMatchingCapabilities: (
  commonName: string,
  selector: string
) => Promise<ExtendedCapability[]> = async (commonName, selector) => {
  const res = await fetch(
    `/api/${commonName}/network/capabilities?selector=${selector}`
  );

  if (res.ok) {
    const capabilities: ExtendedCapability[] = await res.json();
    return capabilities;
  } else {
    const errorObj = await res.json();
    throw new Error(`${errorObj.errorCode}: ${errorObj.message}`);
  }
};

const useMatchingCapabilities = (commonName: string, selector: string) => {
  return useQuery({
    queryKey: ["matchingCapabilities"],
    queryFn: () => fetchMatchingCapabilities(commonName, selector),
  });
};

export { useMatchingCapabilities };
