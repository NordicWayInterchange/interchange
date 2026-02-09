import { useQuery } from "@tanstack/react-query";
import { BiQueueEndpointsApi } from "@/types/napcore/biQueueResponse";

const fetchBiQueueEndpoints: () => Promise<BiQueueEndpointsApi[]> = async () => {
  const res = await fetch(`/api/biqueueendpoints`);

  if (res.ok) {
    return res.json();
  } else {
    const errorObj = await res.json();
    throw new Error(`${errorObj.errorCode}: ${errorObj.message}`);
  }
};

const useBiQueueEndpoints = () => {
  return useQuery({
    queryKey: ["biqueueendpoints"],
    queryFn: () => fetchBiQueueEndpoints(),
  });
};

export { useBiQueueEndpoints };