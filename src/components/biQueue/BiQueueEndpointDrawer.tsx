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
import { useBiQueueEndpoint } from "@/hooks/useBiQueueEndpoint";

type Props = {
  open: boolean;
  onClose: () => void;
};

const BiQueueEndpointDrawer= ({ open , onClose}: Props) => {

  const { data: biQueueEndpoint, refetch} = useBiQueueEndpoint();

  useEffect(() => {
    if (open) {
      refetch();
    }
  }, [open, refetch]);

  return (
    <>
      {biQueueEndpoint &&
        Object.values(biQueueEndpoint).every(
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
                    value={biQueueEndpoint.brokerExternalName}
                    label="Broker External Name"
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
                    label="Message Channel Port"
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
        )}
    </>
  );
};

export default BiQueueEndpointDrawer;

