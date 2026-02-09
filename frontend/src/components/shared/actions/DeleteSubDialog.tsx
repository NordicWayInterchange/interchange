import * as React from "react";
import {
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Divider,
} from "@mui/material";
import {
  deleteDeliveries,
  deleteMyselfFromSubscribedPrivateChannel, deletePrivateChannel,
  deleteSubscriptions,
  deleteUserCapability
} from "@/lib/fetchers/internalFetchers";
import Snackbar from "@/components/shared/feedback/Snackbar";
import { useState } from "react";
import { IFeedback } from "@/interface/IFeedback";
import { StyledButton } from "@/components/shared/styles/StyledSelectorBuilder";

type Props = {
  actorCommonName: string;
  open: boolean;
  handleDialog: (close: boolean) => void;
  itemId: string;
  handleDeletedItem: (deleted: boolean) => void;
  text: string;
  handleMoreClose: () => void;
};

async function deleteArtifacts(artifactType: string, name: string, itemId: string) {
  return await (artifactType === "Delivery" ? deleteDeliveries(name, itemId) :
    artifactType === "Subscription" ? deleteSubscriptions(name, itemId) :
      artifactType === "Private channel" ? deletePrivateChannel(name, itemId) :
      artifactType === "Subscribed private channel" ? deleteMyselfFromSubscribedPrivateChannel(name, itemId) :
        deleteUserCapability(name, itemId));
}

export default function DeleteSubDialog(props: Props) {
  const { actorCommonName, open, handleDialog, itemId, handleDeletedItem, text, handleMoreClose } = props;

  const [feedback, setFeedback] = useState<IFeedback>({
    feedback: false,
    message: "",
    severity: "success",
  });

  const handleDeletion = async (name: string, itemId: string, text: string) => {
    const response = await deleteArtifacts(text, name, itemId);
    handleDialog(false);

    if (response.ok) {
      handleDeletedItem(true);
      setFeedback({
        feedback: true,
        message: `${text} successfully deleted`,
        severity: "success",
      });
    } else {
      const errorData = await response.json();
      const errorMessage = errorData.message || `${text} could not be deleted, try again!`;

      setFeedback({
        feedback: true,
        message: errorMessage,
        severity: "warning",
      });
    }
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
        <DialogTitle>Remove {text}</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Are you sure you want to remove this {text}?
          </DialogContentText>
          <DialogContentText>This action can not be undone.</DialogContentText>
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
            color="depricatedLight"
            onClick={() => handleDeletion(actorCommonName, itemId, text)}
            disableElevation
          >
            Yes, remove
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
