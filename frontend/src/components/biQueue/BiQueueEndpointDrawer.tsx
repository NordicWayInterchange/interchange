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
import { BiQueueEndpointsApi } from "@/types/napcore/biQueueResponse";

type Props = {
  open: boolean;
  onClose: () => void;
  biqueueEndpointRow: BiQueueEndpointsApi
};

const BiQueueEndpointDrawer= ({ open , onClose, biqueueEndpointRow}: Props) => {

  const biqueueEndpoint = biqueueEndpointRow.biqueueEndpointResponse;

  return (
    <>
      {biqueueEndpointRow &&
        Object.values(biqueueEndpointRow).every(
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
                <Typography>Bi-queue endpoint details</Typography>
              </StyledHeaderBox>
            </ListItem>
            <ListItem>
              <StyledCard variant={"outlined"}>
                <Typography>Endpoint</Typography>
                <FormControl fullWidth>
                  <TextField
                    value={biqueueEndpoint.brokerExternalName}
                    label="Host"
                    margin="normal"
                    slotProps={{
                      input: {
                        endAdornment: (
                          <InputAdornment position="end">
                            <ContentCopy value={biqueueEndpoint.brokerExternalName} />
                          </InputAdornment>
                        ),
                      },
                    }}
                  />
                  <TextField
                    value={biqueueEndpoint.messageChannelPort}
                    label="Port"
                    margin="normal"
                    slotProps={{
                      input: {
                        endAdornment: (
                          <InputAdornment position="end">
                            <ContentCopy value={biqueueEndpoint.messageChannelPort?.toString()} />
                          </InputAdornment>
                        ),
                      },
                    }}
                  />
                    <TextField
                      value={biqueueEndpoint.queueName}
                      label="Source"
                      margin="normal"
                      slotProps={{
                        input: {
                          endAdornment: (
                            <InputAdornment position="end">
                              <ContentCopy value={biqueueEndpoint.queueName} />
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

