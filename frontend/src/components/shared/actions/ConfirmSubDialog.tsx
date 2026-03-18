import * as React from "react";
import {
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Divider, IconButton, Tooltip
} from "@mui/material";
import Snackbar from "@/components/shared/feedback/Snackbar";
import { useState } from "react";
import { IFeedback } from "@/interface/IFeedback";
import { StyledButton } from "@/components/shared/styles/StyledSelectorBuilder";
import { HandleCreateSubscription } from "@/components/shared/utils/HandleCreateSubscription";
import InfoOutlinedIcon from "@mui/icons-material/InfoOutlined";

type Props = {
  actorCommonName: string;
  open: boolean;
  handleDialog: (close: boolean) => void;
  handleMoreClose: () => void;
  selector: string;
  description: string;
  text: string;
  form: string;
};

export default function ConfirmSubDialog(props: Props) {
  const {open, handleDialog, handleMoreClose, actorCommonName, selector, description, text, form } = props;

  const [feedback, setFeedback] = useState<IFeedback>({
    feedback: false,
    message: "",
    severity: "success",
  });


  const clickedCreateSubscription = async (name: string, selector: string, description: string) => {
    handleDialog(false);
    if (description.length > 255 ) return ;
    await HandleCreateSubscription(name, setFeedback, selector, description, form);
    handleMoreClose();
  };

  const handleSnackClose = (
    _event?: React.SyntheticEvent | Event,
    reason?: string
  ) => {
    if (reason === "clickaway") {
      return;
    }

    setFeedback({ feedback: false, message: "", severity: "success" });
  };

  return (
    <>
      <Dialog open={open} onClose={() => handleDialog(false)}>
        <DialogTitle>Subscription acknowledgment</DialogTitle>
        <DialogContent>
          <DialogContentText>
            {text}
            <Tooltip
              title={
                <span style={{ fontSize: ".88rem"}}>
                  The sharded capability has large volume of messages
                </span>
              }
              arrow
              placement="right"
            >
              <IconButton size="small">
                <InfoOutlinedIcon fontSize="small" sx={{ mt: -2 }} />
              </IconButton>
            </Tooltip>
          </DialogContentText>
        </DialogContent>
        <Divider />
        <DialogActions sx={{ display: "flex", justifyContent: "space-evenly" }}>
          <StyledButton
            variant="outlined"
            color="inherit"
            onClick={() => handleDialog(false)}
          >
            Cancel
          </StyledButton>
          <StyledButton
            variant="contained"
            color="greenLight"
            onClick={() => clickedCreateSubscription(actorCommonName, selector, description)}
            disableElevation
          >
            Yes, subscribe
          </StyledButton>
        </DialogActions>
      </Dialog>
      {feedback.feedback && (
        <Snackbar
          message={feedback.message}
          severity={feedback.severity}
          open={feedback.feedback}
          handleClose={handleSnackClose}
        />
      )}
    </>
  );
}
