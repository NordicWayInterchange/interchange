import { IconButton, InputAdornment } from "@mui/material";
import ContentCopyIcon from "@mui/icons-material/ContentCopy";
import React, { useState } from "react";
import Snackbar from "@/components/shared/feedback/Snackbar";

type Props = {
  value: string;
};

export const ContentCopy = (props: Props) => {
  const { value } = props;

  const [openSnack, setOpenSnack] = useState<boolean>(false);

  const handleSnackClose = (
    _event?: React.SyntheticEvent | Event,
    reason?: string
  ) => {
    if (reason === "clickaway") {
      return;
    }

    setOpenSnack(false);
  };

  const useClipboard = () => {
    navigator.clipboard.writeText(value).then(
      () => {},
      (error) => {
        alert("Failed to copy text to clipboard: " + error.message);
      }
    );

    setOpenSnack(true);
  };

  return (
    <>
      <InputAdornment position="end">
        <IconButton onClick={useClipboard} edge="end">
          <ContentCopyIcon />
        </IconButton>
      </InputAdornment>
      <Snackbar
        message={"Copied to clipboard"}
        severity={"success"}
        open={openSnack}
        handleClose={handleSnackClose}
      />
    </>
  );
};
