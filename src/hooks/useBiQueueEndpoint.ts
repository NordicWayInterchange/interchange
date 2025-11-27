import { useQuery } from "@tanstack/react-query";
import { BiQueueEndpointResponse } from "@/types/napcore/biQueueResponse";

const fetchBiQueueEndpoint: () => Promise<BiQueueEndpointResponse> = async () => {
  const res = await fetch(`/api/biqueueendpoint`);
  if (res.ok) {
    return res.json();
  } else {
    const errorObj = await res.json();
    throw new Error(`${errorObj.errorCode}: ${errorObj.message}`);
  }
};

const useBiQueueEndpoint = () => {
  return useQuery({
    queryKey: ["biqueueendpoint"],
    queryFn: () => fetchBiQueueEndpoint(),
  });
};

export { useBiQueueEndpoint };