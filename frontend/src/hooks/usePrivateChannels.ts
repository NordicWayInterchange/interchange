import { useQuery } from "@tanstack/react-query";
import { PrivateChannel } from "@/types/napcore/privateChannel";

const fetchPrivateChannels: (
  commonName: string
) => Promise<PrivateChannel[]> = async (commonName) => {
  const res = await fetch(
    `/api/${commonName}/private-channels`
  );

  if (res.ok) {
    return await res.json();
  } else {
    const errorObj = await res.json();
    throw new Error(`${errorObj.status}: ${errorObj.error}`);
  }
};

const usePrivateChannels = (commonName: string) => {
  return useQuery({
    queryKey: ["privateChannels"],
    queryFn: () => fetchPrivateChannels(commonName),
  });
};

export { usePrivateChannels };
