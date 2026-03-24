import {
  Box, Divider,
  Drawer, FormControl, FormControlLabel, IconButton,
  List,
  ListItem, Switch, TextField,
  Toolbar, Tooltip, Typography
} from "@mui/material";
import React, { useEffect, useState } from "react";
import { ExtendedCapability } from "@/types/capability";
import { useSession } from "next-auth/react";
import DeleteSubDialog from "@/components/shared/actions/DeleteSubDialog";
import MapDialog from "@/components/map/MapDialog";
import CapabilityDrawerForm from "@/components/layout/CapabilityDrawerForm";
import { createDelivery } from "@/lib/fetchers/internalFetchers";
import { IFeedback } from "@/interface/IFeedback";
import Snackbar from "@/components/shared/feedback/Snackbar";
import { drawerStyle, StyledCard, StyledButton } from "@/components/shared/styles/StyledSelectorBuilder";
import { handleDescription } from "@/lib/handleDescription";
import { tooltipFontStyle } from "@/components/shared/styles/TooltipFontStyle";
import InfoOutlinedIcon from "@mui/icons-material/InfoOutlined";

type Props = {
  capability: ExtendedCapability;
  open: boolean;
  handleMoreClose: () => void;
  handleDeletedItem: (deleted: boolean) => void;
  onSwitchChange: (checked: boolean) => void;
};

const UserCapabilitiesDrawer = ({ capability, open, handleMoreClose, handleDeletedItem, onSwitchChange }: Props) => {
  const { data: session } = useSession();
  const [openMap, setOpenMap] = useState<boolean>(false);
  const [dialogOpen, setDialogOpen] = useState<boolean>(false);
  const [feedback, setFeedback] = useState<IFeedback>({
    feedback: false,
    message: "",
    severity: "success",
  });
  const [descriptionError, setDescriptionError] = useState(false);
  const [description, setDescription] = useState<string>("");
  const selector = `(publicationId = '${capability.publicationId}')`;
  const [dlqueue, setDlqueue] = useState<boolean>(false);

  useEffect(() => {
    setDlqueue(false);
  }, []);

  const handleSnackClose = (
    _event?: React.SyntheticEvent | Event,
    reason?: string
  ) => {
    if (reason === "clickaway") {
      return;
    }

    setFeedback({ feedback: false, message: "", severity: "success" });
  };

  const handleClose = () => {
    setOpenMap(false);
  };

  const handleClickClose = (close: boolean) => {
    setDialogOpen(close);
  };

  const removeDescriptionError = () => {
    setDescription("");
    setDescriptionError(false);
  }

  const enableDlqueue = (event: React.ChangeEvent<HTMLInputElement>) => {
    const isdlqueueChecked = event.target.checked;
    setDlqueue(isdlqueueChecked);
    onSwitchChange(isdlqueueChecked);
  };

  const saveDelivery = async (name: string, selector: string) => {
    if (description.length > 255 ) return ;

    const bodyData = {
      selector: selector,
      description: description,
      dlqueue: dlqueue,
    };

    const response = await createDelivery(name, bodyData);

    if (response.ok) {
      setFeedback({
        feedback: true,
        message: `Delivery successfully created`,
        severity: "success",
      });
    } else {
      if (response.statusText === "Conflict") {
        setFeedback({
          feedback: true,
          message: "Delivery already exists!",
          severity: "warning",
        });
      } else {
        const errorData = await response.json();
        const errorMessage = errorData.message || `Delivery could not be created, try again!`;

        setFeedback({
          feedback: true,
          message: errorMessage,
          severity: "warning",
        });
      }
    }
    handleMoreClose();
  };

  return (
    <>
      <Drawer
        sx={drawerStyle}
        slotProps={{paper: {sx: {backgroundColor: "#F9F9F9"}}}}
        variant="temporary"
        anchor="right"
        open={open}
        onClose={() => {handleMoreClose(); removeDescriptionError(); }}
      >
        <Toolbar />
        <Box sx={{ padding: 1, width: 1 }}>
          <List>
            <CapabilityDrawerForm capability={capability}
                                  handleMoreClose={handleMoreClose}
                                  setOpenMap={setOpenMap}
                                  removeDescriptionError={removeDescriptionError}
                                  setDialogOpen={setDialogOpen}
                                  type="delivery"/>
            <ListItem>
              <StyledCard variant={"outlined"}>
                <Typography sx={{ mb:2 }}>Description to create a delivery</Typography>
                <div>
                  <Divider style={{ margin: '5px 0', visibility: 'hidden' }}/>
                  <FormControl fullWidth>
                    <TextField
                      name="description"
                      label="Enter a description for delivery"
                      multiline
                      rows={4}
                      onChange={(event) =>
                        handleDescription(event, setDescription, setDescriptionError)}
                      error={descriptionError}
                      helperText={descriptionError ? "Description exceeds maximum length of 255 characters" : ""}
                      fullWidth
                    />
                  </FormControl>
                </div>
                <FormControlLabel
                  control={<Switch checked={dlqueue} onChange={enableDlqueue} />}
                  sx={{mt: 1 }}
                  label={
                    <Box display="flex" alignItems="center">
                    <span>
                      Enable dead letter queue (DLQ) for this delivery
                    </span>
                      <Tooltip
                        slotProps={{
                          tooltip: {
                            sx: tooltipFontStyle,
                          },
                        }}
                        title="Messages that couldn't be delivered are moved to dlqueue"
                      >
                        <IconButton size="small">
                          <InfoOutlinedIcon fontSize="small" sx={{ mt: -2 }} />
                        </IconButton>
                      </Tooltip>
                    </Box>
                  }
                />
              </StyledCard>
            </ListItem>

            <ListItem sx={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
              <StyledButton
                variant={"contained"}
                color={"buttonThemeColor"}
                disableElevation
                onClick={() =>
                  saveDelivery(session?.user.commonName as string, selector)
                }
              >
                Deliver
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
        itemId={capability.id as string}
        handleDialog={handleClickClose}
        handleDeletedItem={handleDeletedItem}
        text="Capability"
        handleMoreClose={handleMoreClose}
      />
      <MapDialog
        open={openMap}
        onClose={handleClose}
        quadtree={capability.quadTree}
        interactive={false}
      />
    </>
  );
};

export default UserCapabilitiesDrawer;
