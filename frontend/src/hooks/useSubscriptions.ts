import { ExtendedSubscription } from "@/types/subscription";
import { SubscriptionsSubscription } from "@/types/napcore/subscription";
import { useQuery } from "@tanstack/react-query";

const fetchSubscriptions: (
  commonName: string
) => Promise<ExtendedSubscription[]> = async (commonName: string) => {
  const res = await fetch(`/api/${commonName}/subscriptions`);
  if (res.ok) {
    const subscriptions: SubscriptionsSubscription[] = await res.json();
    const seasonedSubscription = subscriptions.map(async (sub) => {
      const fetchNumberOfCapabilities = await fetch(
        `/api/${commonName}/capability-count/?selector=${sub.selector}`
      );
      if (fetchNumberOfCapabilities.ok) {
        const data = await fetchNumberOfCapabilities.json();
        return { ...sub, capabilityMatches: data };
      } else {
        console.error(
          `error when fetching ${sub.selector} - ${fetchNumberOfCapabilities.status} - ${fetchNumberOfCapabilities.statusText}`
        );
        return { ...sub, capabilityMatches: 0 };
      }
    });
    return Promise.all(seasonedSubscription);
  } else {
    const errorObj = await res.json();
    throw new Error(`${errorObj.status}: ${errorObj.error}`);
  }
};

const useSubscriptions = (commonName: string) => {
  return useQuery({
    queryKey: ["subscriptions"],
    queryFn: () => fetchSubscriptions(commonName),
    //refetchInterval: 1000, // set this to a refetch interval
  });
};

export { useSubscriptions };
