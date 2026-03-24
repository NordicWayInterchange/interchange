import {
  Box, Card,
  Drawer, FormControl, IconButton, InputAdornment,
  List,
  ListItem, ListItemText, TextField,
  Toolbar, Typography
} from "@mui/material";
import React, { useState } from "react";
import { IFeedback } from "@/interface/IFeedback";
import Snackbar from "@/components/shared/feedback/Snackbar";
import { PrivateChannelPeers } from "@/types/napcore/privateChannel";
import CloseIcon from "@mui/icons-material/Close";
import { Chip } from "@/components/shared/display/Chip";
import { statusChips } from "@/lib/statusChips";
import { ContentCopy } from "@/components/shared/actions/ContentCopy";
import { styled } from "@mui/material/styles";
import { timeConverter } from "@/lib/timeConverter";
import DeleteSubDialog from "@/components/shared/actions/DeleteSubDialog";
import { useSession } from "next-auth/react";
import { StyledButton, drawerStyle } from "@/components/shared/styles/StyledSelectorBuilder";
import { StyledHeaderBox } from "@/components/shared/styles/StyledHeaderBox";

type Props = {
  peers: PrivateChannelPeers;
  open: boolean;
  handleMoreClose: () => void;
  handleDeletedItem: (deleted: boolean) => void;
};

const PeersDrawer = ({ peers, open, handleMoreClose, handleDeletedItem }: Props) => {
  const { data: session } = useSession();
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
                <Typography>Private channel subscription details</Typography>
                <Chip
                  color={
                    statusChips[
                      peers.status.toString() as keyof typeof statusChips
                      ] as any
                  }
                  label={peers.status}
                />
              </StyledHeaderBox>
            </ListItem>

            <ListItem>
              <StyledCard variant={"outlined"}>
                <Box sx={{ display: "flex", justifyContent: "space-between" }}>
                  <Box>
                    <ListItemText primary={"ID"} secondary={peers.id} />
                  </Box>
                  <Box>
                    <ListItemText
                      primary={"Last updated"}
                      secondary={timeConverter(peers.lastUpdated)}
                    />
                  </Box>
                </Box>
                <FormControl fullWidth>
                  <TextField
                    value={peers.owner}
                    label={"Owner"}
                    margin="normal"
                    slotProps={{
                      input: {
                        endAdornment: (
                          <InputAdornment position="end">
                            <ContentCopy value={peers.owner} />
                          </InputAdornment>
                        ),
                      },
                    }}
                  />
                </FormControl>
              </StyledCard>
            </ListItem>

            {peers.endpoint && (
              <ListItem>
                <StyledCard variant={"outlined"}>
                  <Typography>Endpoint</Typography>
                  <FormControl fullWidth>
                    <TextField
                      value={peers.endpoint.host}
                      label={"Host"}
                      margin="normal"
                      slotProps={{
                        input: {
                          endAdornment: (
                            <InputAdornment position="end">
                              <ContentCopy value={peers.endpoint.host} />
                            </InputAdornment>
                          ),
                        },
                      }}
                    />
                    <TextField
                      value={peers.endpoint.port}
                      label="Port"
                      margin="normal"
                      slotProps={{
                        input: {
                          endAdornment: (
                            <InputAdornment position="end">
                              <ContentCopy value={peers.endpoint.port.toString()} />
                            </InputAdornment>
                          ),
                        },
                      }}
                    />
                    <TextField
                      value={peers.endpoint.queueName}
                      label="Queue name"
                      margin="normal"
                      slotProps={{
                        input: {
                          endAdornment: (
                            <InputAdornment position="end">
                              <ContentCopy value={peers.endpoint.queueName} />
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
                      value={peers.description || ""}
                      rows={4}
                      slotProps={{
                        input: {
                          endAdornment: (
                            <InputAdornment position="end">
                              <ContentCopy value={peers.description} />
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
                sx={{ width: '263px'}}
              >
                Remove private channel subscription
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
        itemId={peers.id as string}
        handleDialog={handleClickClose}
        open={dialogOpen}
        actorCommonName={session?.user.commonName as string}
        handleDeletedItem={handleDeletedItem}
        text="Subscribed private channel"
        handleMoreClose={handleMoreClose}
      />
    </>
  );
};

const StyledCard = styled(Card)(({}) => ({
  padding: "16px",
  width: "100%"
}));

export default PeersDrawer;
