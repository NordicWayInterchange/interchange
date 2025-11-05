import { useQuery } from "@tanstack/react-query";
import { BiQueue } from "@/types/napcore/biQueue";

const fetchAccessToBiQueue: (
  commonName: string
) => Promise<BiQueue> = async (commonName: string) => {
  const res = await fetch(`/api/${commonName}/bi-consumer`);
  if (res.ok) {
    return res.json();
  } else {
    const errorObj = await res.json();
    throw new Error(`${errorObj.errorCode}: ${errorObj.message}`);
  }
};

const useAccessToBiQueue = (commonName: string) => {
  return useQuery({
    queryKey: ["biQueueAccess"],
    queryFn: () => fetchAccessToBiQueue(commonName),
  });
};

export { useAccessToBiQueue };

