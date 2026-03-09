import { SubscriptionsSubscription } from "@/types/napcore/subscription";

export type ExtendedSubscription = SubscriptionsSubscription & {
  capabilityMatches: number;
};
