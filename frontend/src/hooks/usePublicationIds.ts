import { useQuery } from "@tanstack/react-query";
import { Publicationids } from "@/types/napcore/capability";

const fetchPublicationIds: (
  commonName: string,
) => Promise< Publicationids[]> = async (commonName) => {
  const res = await fetch(
    `/api/${commonName}/capabilities/publicationids`
  );
  if (res.ok) {
    return await res.json();
  } else {
    const errorObj = await res.json();
    throw new Error(`${errorObj.status}: ${errorObj.error}`);
  }
};

const usePublicationIds = (commonName: string) => {
  return useQuery({
    queryKey: ["publicationids"],
    queryFn: () => fetchPublicationIds(commonName),
  });
};

export { usePublicationIds };
