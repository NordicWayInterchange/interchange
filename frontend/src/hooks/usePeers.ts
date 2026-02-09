import { useQuery } from "@tanstack/react-query";
import { PrivateChannelPeers } from "@/types/napcore/privateChannel";

const fetchPeers: (
  commonName: string
) => Promise<PrivateChannelPeers[]> = async (commonName) => {
  const res = await fetch(
    `/api/${commonName}/private-channels/peer`
  );

  if (res.ok) {
    return await res.json();
  } else {
    const errorObj = await res.json();
    throw new Error(`${errorObj.status}: ${errorObj.error}`);
  }
};

const usePeers = (commonName: string) => {
  return useQuery({
    queryKey: ["peers"],
    queryFn: () => fetchPeers(commonName),
  });
};

export { usePeers };
