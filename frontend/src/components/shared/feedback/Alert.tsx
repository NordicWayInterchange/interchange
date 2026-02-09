import React from "react";
import { Alert as MuiAlert, AlertProps, AlertTitle } from "@mui/material";

interface Props extends AlertProps {
  title: string;
  information: string;
}

export default function Alert({ information, severity, title }: Props) {
  return (
    <MuiAlert severity={severity}>
      <AlertTitle>{title}</AlertTitle>
      {information}
    </MuiAlert>
  );
}
