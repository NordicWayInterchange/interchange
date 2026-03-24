import { ExtendedCapability } from "@/types/capability";
import { useQuery } from "@tanstack/react-query";

const fetchUserCapabilities: (
  commonName: string
) => Promise<ExtendedCapability[]> = async (commonName: string) => {
  const res = await fetch(`/api/${commonName}/user/capabilities`);
  if (res.ok) {
    return res.json();
  } else {
    const errorObj = await res.json();
    throw new Error(`${errorObj.errorCode}: ${errorObj.message}`);
  }
};

const useUserCapabilities = (commonName: string) => {
  return useQuery({
    queryKey: ["capabilities"],
    queryFn: () => fetchUserCapabilities(commonName),
  });
};

export { useUserCapabilities };

