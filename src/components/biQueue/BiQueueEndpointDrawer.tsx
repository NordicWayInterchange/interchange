import { BiQueueEndpointResponse } from "@/types/napcore/biQueueResponse";
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
import React from "react";
import { ContentCopy } from "@/components/shared/actions/ContentCopy";
import CloseIcon from "@mui/icons-material/Close";
import { StyledHeaderBox } from "@/components/shared/styles/StyledHeaderBox";

type Props = {
  biQueueEndpoint: BiQueueEndpointResponse;
  open: boolean;
  onClose: () => void;
};

const BiQueueEndpointDrawer= ({ biQueueEndpoint, open , onClose}: Props) => {

  return (
    <>
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
                    value={biQueueEndpoint.brokerExternalName}
                    label="Broker Name"
                    margin="normal"
                    slotProps={{
                      input: {
                        endAdornment: (
                          <InputAdornment position="end">
                            <ContentCopy value={biQueueEndpoint.brokerExternalName} />
                          </InputAdornment>
                        ),
                      },
                    }}
                  />
                  <TextField
                    value={biQueueEndpoint.messageChannelPort}
                    label="Port"
                    margin="normal"
                    slotProps={{
                      input: {
                        endAdornment: (
                          <InputAdornment position="end">
                            <ContentCopy value={biQueueEndpoint.messageChannelPort?.toString()} />
                          </InputAdornment>
                        ),
                      },
                    }}
                  />
                    <TextField
                      value={biQueueEndpoint.queueName}
                      label="Queue Name"
                      margin="normal"
                      slotProps={{
                        input: {
                          endAdornment: (
                            <InputAdornment position="end">
                              <ContentCopy value={biQueueEndpoint.queueName} />
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
    </>
  );
};

export default BiQueueEndpointDrawer;

