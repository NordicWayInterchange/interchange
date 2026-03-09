import {
  Box,
  Card,
  Drawer, FormControl, IconButton, InputAdornment,
  List,
  ListItem, ListItemText, TextField,
  Toolbar, Typography
} from "@mui/material";
import React, { useState } from "react";
import { useSession } from "next-auth/react";
import DeleteSubDialog from "@/components/shared/actions/DeleteSubDialog";
import { IFeedback } from "@/interface/IFeedback";
import Snackbar from "@/components/shared/feedback/Snackbar";
import { PrivateChannel } from "@/types/napcore/privateChannel";
import CloseIcon from "@mui/icons-material/Close";
import { Chip } from "@/components/shared/display/Chip";
import { statusChips } from "@/lib/statusChips";
import { ContentCopy } from "@/components/shared/actions/ContentCopy";
import { styled } from "@mui/material/styles";
import { timeConverter } from "@/lib/timeConverter";
import CollapsiblePeer from "@/components/shared/display/CollapsiblePeer";
import { StyledButton, drawerStyle } from "@/components/shared/styles/StyledSelectorBuilder";
import { StyledHeaderBox } from "@/components/shared/styles/StyledHeaderBox";

type Props = {
  privateChannel: PrivateChannel;
  open: boolean;
  handleMoreClose: () => void;
  handleDeletedItem: (deleted: boolean) => void;
  refetchPrivateChannel: () => void;
};

const PrivateChannelsDrawer = ({ privateChannel, open, handleMoreClose, handleDeletedItem, refetchPrivateChannel }: Props) => {
  const {data: session } = useSession();
  const [dialogOpen, setDialogOpen] = useState<boolean>(false);
  const [feedback, setFeedback] = useState<IFeedback>({
    feedback: false,
    message: "",
    severity: "success",
  });

  const handleSnackClose = (
    _event?: React.SyntheticEvent | Event,
    reason?: string
  ) => {
    if (reason === "clickaway") {
      return;
    }

    setFeedback({ feedback: false, message: "", severity: "success" });
  };


  const handleClickClose = (close: boolean) => {
    setDialogOpen(close);
  };

  return (
    <>
      <Drawer
        sx={drawerStyle}
        slotProps={{paper: {sx: {backgroundColor: "#F9F9F9"}}}}
        variant="temporary"
        anchor="right"
        open={open}
        onClose={() => handleMoreClose()}
      >
        <Toolbar />
        <Box sx={{ padding: 1, width: 1 }}>
          <List>

            <ListItem sx={{ justifyContent: "flex-end" }}>
              <IconButton onClick={handleMoreClose}>
                <CloseIcon />
              </IconButton>
            </ListItem>
            <ListItem>
              <StyledHeaderBox>
                <Typography>Private channel details</Typography>
                <Chip
                  color={
                    statusChips[
                      privateChannel.status.toString() as keyof typeof statusChips
                      ] as any
                  }
                  label={privateChannel.status}
                />
              </StyledHeaderBox>
            </ListItem>

            <ListItem>
              <StyledCard variant={"outlined"}>
                <Box sx={{ display: "flex", justifyContent: "space-between" }}>
                  <Box>
                    <ListItemText primary={"ID"} secondary={privateChannel.id} />
                  </Box>
                  <Box>
                    <ListItemText
                      primary={"Last updated"}
                      secondary={timeConverter(privateChannel.lastUpdated)}
                    />
                  </Box>
                </Box>
              </StyledCard>
            </ListItem>

            {privateChannel.peers && (
              <ListItem>
                <StyledCard variant={"outlined"}>
                  <Typography  sx={{ marginBottom: 2 }}>Peers</Typography>
                  <FormControl fullWidth>
                    <CollapsiblePeer
                      subItems={privateChannel.peers}
                      privateChannelId={privateChannel.id as string}
                      actorCommonName={session?.user.commonName as string}
                      refetchPrivateChannel={refetchPrivateChannel}/>
                  </FormControl>
                </StyledCard>
              </ListItem>
            )}
            {privateChannel.endpoint && (
              <ListItem>
                <StyledCard variant={"outlined"}>
                  <Typography>Endpoint</Typography>
                  <FormControl fullWidth>
                    <TextField
                      value={privateChannel.endpoint.host}
                      label={"Host"}
                      margin="normal"
                      slotProps={{
                        input: {
                          endAdornment: (
                            <InputAdornment position="end">
                              <ContentCopy value={privateChannel.endpoint.host} />
                            </InputAdornment>
                          ),
                        },
                      }}
                    />
                    <TextField
                      value={privateChannel.endpoint.port}
                      label="Port"
                      margin="normal"
                      slotProps={{
                        input: {
                          endAdornment: (
                            <InputAdornment position="end">
                              <ContentCopy value={privateChannel.endpoint.port.toString()} />
                            </InputAdornment>
                          ),
                        },
                      }}
                    />
                    <TextField
                      value={privateChannel.endpoint.queueName}
                      label="Queue name"
                      margin="normal"
                      slotProps={{
                        input: {
                          endAdornment: (
                            <InputAdornment position="end">
                              <ContentCopy value={privateChannel.endpoint.queueName} />
                            </InputAdornment>
                          ),
                        },
                      }}
                    />
                  </FormControl>
                </StyledCard>
              </ListItem>
            )}
            <ListItem>
              <StyledCard variant={"outlined"}>
                <Typography>Description</Typography>
                <div>
                  <FormControl fullWidth>
                    <TextField
                      margin="normal"
                      multiline
                      value={privateChannel.description || ""}
                      rows={4}
                      slotProps={{
                        input: {
                          endAdornment: (
                            <InputAdornment position="end">
                              <ContentCopy value={privateChannel.description} />
                            </InputAdornment>
                          ),
                        },
                      }}
                    />
                  </FormControl>
                </div>
              </StyledCard>
            </ListItem>
            <ListItem >
              <StyledButton
                variant={"contained"}
                color={"redLight"}
                onClick={() => setDialogOpen(true)}
                disableElevation
              >
                Remove private channel
              </StyledButton>
            </ListItem>
          </List>
        </Box>
      </Drawer>
      {feedback.feedback && (
        <Snackbar
          message={feedback.message}
          severity={feedback.severity}
          open={feedback.feedback}
          handleClose={handleSnackClose}
        />
      )}
      <DeleteSubDialog
        open={dialogOpen}
        actorCommonName={session?.user.commonName as string}
        itemId={privateChannel.id as string}
        handleDialog={handleClickClose}
        handleDeletedItem={handleDeletedItem}
        text="Private channel"
        handleMoreClose={handleMoreClose}
      />
    </>
  );
};

const StyledCard = styled(Card)(({}) => ({
  padding: "16px",
  width: "100%"
}));

export default PrivateChannelsDrawer;
