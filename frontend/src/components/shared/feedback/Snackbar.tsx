import React from "react";
import {
  Alert,
  AlertColor,
  Snackbar as MuiSnackbar,
  SnackbarProps,
} from "@mui/material";

interface Props extends SnackbarProps {
  open: boolean;
  message: string | string[];
  severity: AlertColor;
  handleClose: (event?: React.SyntheticEvent | Event, reason?: string) => void;
}

const Snackbar = (props: Props) => {
  const { open, handleClose, severity, message } = props;
  const messages = Array.isArray(message) ? message : [message];
  return (
    <MuiSnackbar
      anchorOrigin={{ vertical: "bottom", horizontal: "center" }}
      open={open}
      autoHideDuration={4000}
      onClose={(event, reason) => handleClose(event ?? undefined, reason)}
    >
      <Alert
        elevation={6}
        variant="filled"
        onClose={handleClose}
        severity={severity}
        sx={{ width: "100%" }}
      >
        {messages.map((line, index) => (
          <div key={index}>{line}</div>
        ))}
      </Alert>
    </MuiSnackbar>
  );
};

export default Snackbar;
