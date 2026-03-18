import { ExtendedCapability } from "@/types/capability";
import { useQuery } from "@tanstack/react-query";

const fetchCapabilities: (
  commonName: string,
  selector?: string
) => Promise<ExtendedCapability[]> = async (commonName, selector = "") => {
  const res = await fetch(
    `/api/${commonName}/network/capabilities?selector=${selector}`
  );

  if (res.ok) {
    return await res.json();
  } else {
    const errorObj = await res.json();
    throw new Error(`${errorObj.status}: ${errorObj.error}`);
  }
};

const useNetworkCapabilities = (commonName: string) => {
  return useQuery({
    queryKey: ["networkCapabilities"],
    queryFn: () => fetchCapabilities(commonName),
  });
};

export { useNetworkCapabilities };
