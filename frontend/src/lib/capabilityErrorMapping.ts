import { IFormCapabilityInputs } from "@/interface/IFormCapabilityInputs";
import { ICapabilityErrorMessage } from "@/interface/ICapabilityValidationError";

export const CAPABILITY_FIELD_NAMES: (keyof IFormCapabilityInputs)[] = [
  "publisherId",
  "publicationId",
  "originatingCountry",
  "protocolVersion",
  "quadTree",
  "publisherName",
  "publicationType",
  "causeCode",
  "messageType",
];

export const mapErrorMessageToField = (
  message: string
): keyof IFormCapabilityInputs | undefined => {
  return CAPABILITY_FIELD_NAMES.find((field) => message.startsWith(field));
};

export const formatCapabilityError = (error: ICapabilityErrorMessage): string => {
  return `${error.code}: ${error.message}`;
};

