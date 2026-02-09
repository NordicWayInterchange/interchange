import { Application } from "./napcore/capability";

export type ExtendedCapability = Application & {
  id: string;
  causeCodesDictionary: Array<causeCodesDictionary>;
};

type causeCodesDictionary = {
  value: number;
  label: string;
};
