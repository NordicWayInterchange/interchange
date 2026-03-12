import { DeliveriesDelivery } from "./napcore/delivery";

export type ExtendedDelivery = DeliveriesDelivery & {
  capabilityMatches: number;
};
