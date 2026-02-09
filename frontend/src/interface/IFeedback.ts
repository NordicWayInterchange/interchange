import { AlertColor } from "@mui/material";

export interface IFeedback {
  feedback: boolean;
  message: string;
  severity: AlertColor;
}
