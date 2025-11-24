import { BiQueueEndpointResponse } from "@/types/napcore/biQueueResponse";
import {
  Box,
  Drawer,
  FormControl,
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

type Props = {
  biQueueEndpoint: BiQueueEndpointResponse;
  open: boolean;
  handleMoreClose: () => void;
};

const BiQueueEndpointDrawer= ({ biQueueEndpoint, open, handleMoreClose }: Props) => {

  return (
    <>
      <Drawer
        sx={drawerStyle}
        slotProps={{paper: {sx: {backgroundColor: "#F9F9F9"}}}}
        variant="temporary"
        anchor="right"
        open={open}
        onClose={() => {
          handleMoreClose();
        }}
      >
        <Toolbar />
        <Box sx={{ padding: 1, width: 1 }}>
          <List>
            <ListItem>
              <StyledCard variant={"outlined"}>
                <Typography>Bi-queue endpoint</Typography>
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
                            <ContentCopy value={biQueueEndpoint.messageChannelPort.toString()} />
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

