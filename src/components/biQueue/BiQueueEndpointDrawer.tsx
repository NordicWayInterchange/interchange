import {
  Box,
  Drawer,
  FormControl, IconButton,
  InputAdornment,
  List,
  ListItem,
  TextField,
  Toolbar,
  Typography
} from "@mui/material";
import { drawerStyle, StyledCard } from "@/components/shared/styles/StyledSelectorBuilder";
import React, { useEffect } from "react";
import { ContentCopy } from "@/components/shared/actions/ContentCopy";
import CloseIcon from "@mui/icons-material/Close";
import { StyledHeaderBox } from "@/components/shared/styles/StyledHeaderBox";
import { BiQueueEndpointResponse } from "@/types/napcore/biQueueResponse";

type Props = {
  open: boolean;
  onClose: () => void;
  biQueueEndpoints: BiQueueEndpointResponse
};

const BiQueueEndpointDrawer= ({ open , onClose, biQueueEndpoints}: Props) => {

  return (
    <>
      {biQueueEndpoints &&
        Object.values(biQueueEndpoints).every(
          (v) => v !== null && v !== undefined,
        ) && (
      <Drawer
        sx={drawerStyle}
        slotProps={{paper: {sx: {backgroundColor: "#F9F9F9"}}}}
        variant="temporary"
        anchor="right"
        open={open}
        onClose={onClose}
      >
        <Toolbar />
        <Box sx={{ padding: 1 }}>
          <List>
            <ListItem sx={{ justifyContent: "flex-end" }}>
              <IconButton onClick={onClose}>
                <CloseIcon />
              </IconButton>
            </ListItem>
            <ListItem>
              <StyledHeaderBox>
                <Typography>Bi-queue details</Typography>
              </StyledHeaderBox>
            </ListItem>
            <ListItem>
              <StyledCard variant={"outlined"}>
                <Typography>Endpoint</Typography>
                <FormControl fullWidth>
                  <TextField
                    value={biQueueEndpoints.brokerExternalName}
                    label="Host"
                    margin="normal"
                    slotProps={{
                      input: {
                        endAdornment: (
                          <InputAdornment position="end">
                            <ContentCopy value={biQueueEndpoints.brokerExternalName} />
                          </InputAdornment>
                        ),
                      },
                    }}
                  />
                  <TextField
                    value={biQueueEndpoints.messageChannelPort}
                    label="Port"
                    margin="normal"
                    slotProps={{
                      input: {
                        endAdornment: (
                          <InputAdornment position="end">
                            <ContentCopy value={biQueueEndpoints.messageChannelPort?.toString()} />
                          </InputAdornment>
                        ),
                      },
                    }}
                  />
                    <TextField
                      value={biQueueEndpoints.queueName}
                      label="Source"
                      margin="normal"
                      slotProps={{
                        input: {
                          endAdornment: (
                            <InputAdornment position="end">
                              <ContentCopy value={biQueueEndpoints.queueName} />
                            </InputAdornment>
                          ),
                        },
                      }}
                    />
                </FormControl>
              </StyledCard>
            </ListItem>
          </List>
        </Box>
      </Drawer>
        )}
    </>
  );
};

export default BiQueueEndpointDrawer;

