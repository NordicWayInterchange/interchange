export interface ICapabilityErrorMessage {
  code: string;
  message: string;
}

export interface ICapabilityValidationErrorResponse {
  timestamp: string;
  message: string;
  errors: ICapabilityErrorMessage[];
}

