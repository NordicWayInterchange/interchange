import { useQuery } from "@tanstack/react-query";
import { BiQueueEndpointResponse, BiQueueResponse } from "@/types/napcore/biQueueResponse";

const fetchBiQueueEndpoint: (
  commonName: string
) => Promise<BiQueueEndpointResponse> = async (commonName: string) => {
  const res = await fetch(`/api/${commonName}/biqueueendpoint`);
  if (res.ok) {
    return res.json();
  } else {
    const errorObj = await res.json();
    throw new Error(`${errorObj.errorCode}: ${errorObj.message}`);
  }
};

const useBiQueueEndpoint = (commonName: string) => {
  return useQuery({
    queryKey: ["biqueueendpoint"],
    queryFn: () => fetchBiQueueEndpoint(commonName),
  });
};

export { useBiQueueEndpoint };