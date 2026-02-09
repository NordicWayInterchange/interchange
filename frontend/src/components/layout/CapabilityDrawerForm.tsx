import {
  Card,
  FormControl,
  IconButton,
  InputAdornment,
  InputLabel,
  ListItem,
  MenuItem,
  Select,
  TextField,
  Typography,
} from "@mui/material";
import CloseIcon from "@mui/icons-material/Close";
import { ContentCopy } from "@/components/shared/actions/ContentCopy";
import React from "react";
import { ExtendedCapability } from "@/types/capability";
import { styled } from "@mui/material/styles";
import { StyledButton } from "@/components/shared/styles/StyledSelectorBuilder";

type Props = {
  capability: ExtendedCapability;
  handleMoreClose: () => void;
  removeDescriptionError: () => void;
  setOpenMap: (open: boolean) => void;
  setDialogOpen: (open: boolean) => void;
  type: string;
};
const CapabilityDrawerForm = ({
  capability,
  handleMoreClose,
  removeDescriptionError,
  setOpenMap,
  setDialogOpen,
  type
}: Props) => {
  return (
    <>
      <ListItem sx={{ justifyContent: "flex-end" }}>
        <IconButton
          onClick={() => {
            handleMoreClose();
            removeDescriptionError();
          }}
        >
          <CloseIcon />
        </IconButton>
      </ListItem>
      <ListItem>
        <Typography>Capability details</Typography>
      </ListItem>

      <ListItem>
        <StyledCard variant={"outlined"}>
          <Typography>Publisher</Typography>
          <FormControl fullWidth>
            <TextField
              value={capability.publisherId}
              label="Publisher ID"
              margin="normal"
              slotProps={{
                input: {
                  endAdornment: (
                    <InputAdornment position="end">
                      <ContentCopy value={capability.publisherId} />
                    </InputAdornment>
                  ),
                },
              }}
            />
            <TextField
              value={capability.publicationId}
              label="Publication ID"
              margin="normal"
              slotProps={{
                input: {
                  endAdornment: (
                    <InputAdornment position="end">
                      <ContentCopy value={capability.publicationId} />
                    </InputAdornment>
                  ),
                },
              }}
            />
            {capability.publicationType && (
              <TextField
                value={capability.publicationType}
                label="Publication type"
                margin="normal"
                slotProps={{
                  input: {
                    endAdornment: (
                      <InputAdornment position="end">
                        <ContentCopy value={capability.publicationType} />
                      </InputAdornment>
                    ),
                  },
                }}
              />
            )}
            {capability.publisherName && (
              <TextField
                value={capability.publisherName}
                label="Publisher name"
                margin="normal"
                slotProps={{
                  input: {
                    endAdornment: (
                      <InputAdornment position="end">
                        <ContentCopy value={capability.publisherName} />
                      </InputAdornment>
                    ),
                  },
                }}
              />
            )}
            <TextField
              value={capability.originatingCountry}
              label="Originating Country"
              margin="normal"
              slotProps={{
                input: {
                  endAdornment: (
                    <InputAdornment position="end">
                      <ContentCopy value={capability.originatingCountry} />
                    </InputAdornment>
                  ),
                },
              }}
            />
          </FormControl>
        </StyledCard>
      </ListItem>
      <ListItem>
        <StyledCard variant={"outlined"}>
          <Typography>Message</Typography>
          <FormControl fullWidth>
            <TextField
              value={capability.messageType}
              label="Message Type"
              margin="normal"
              slotProps={{
                input: {
                  endAdornment: (
                    <InputAdornment position="end">
                      <ContentCopy value={capability.messageType} />
                    </InputAdornment>
                  ),
                },
              }}
            />
            <TextField
              value={capability.protocolVersion}
              label="Protocol Version"
              margin="normal"
              slotProps={{
                input: {
                  endAdornment: (
                    <InputAdornment position="end">
                      <ContentCopy value={capability.protocolVersion} />
                    </InputAdornment>
                  ),
                },
              }}
            />
            {capability.causeCodesDictionary && (
              <FormControl margin="normal">
                <InputLabel>Cause codes</InputLabel>
                <Select
                  MenuProps={{ PaperProps: { sx: { maxHeight: 200 } } }}
                  label="Cause codes"
                  multiple
                  defaultValue={capability.causeCodesDictionary.map((cause) => {
                    return cause["value"];
                  })}
                >
                  {capability.causeCodesDictionary.map((cause, index) => {
                    return (
                      <StyledMenuItem disabled key={index} value={cause.value}>
                        {cause.value}
                        {cause.label ? ":" : ""} {cause.label}
                      </StyledMenuItem>
                    );
                  })}
                </Select>
              </FormControl>
            )}
          </FormControl>
        </StyledCard>
      </ListItem>
      {capability.shardCount > 1 && (
        <ListItem>
          <StyledCard variant={"outlined"}>
            <Typography>Shards</Typography>
            <FormControl fullWidth>
              <TextField
                value={capability.shardCount}
                label="Number of shards"
                margin="normal"
                slotProps={{
                  input: {
                    endAdornment: (
                      <InputAdornment position="end">
                        <ContentCopy value={capability.shardCount.toString()} />
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
          <Typography>Quadtree</Typography>
          <FormControl
            fullWidth
            sx={{
              display: "flex",
              flexDirection: "row",
              alignItems: "center",
            }}
          >
            <TextField
              value={capability.quadTree}
              label="Hash"
              margin="normal"
              sx={{
                flexGrow: 1,
                marginRight: 1,
              }}
            />
            <StyledButton
              sx={{ mt: 2.75 }}
              color="buttonThemeColor"
              variant="outlined"
              onClick={() => setOpenMap(true)}
            >
              Show map
            </StyledButton>
          </FormControl>
        </StyledCard>
      </ListItem>

      {type === "delivery" && (
        <ListItem
          sx={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
          }}
        >
          <StyledButton
            variant={"contained"}
            color={"redLight"}
            onClick={() => setDialogOpen(true)}
            disableElevation
          >
            Remove Capability
          </StyledButton>
        </ListItem>
      )}
    </>
  );
};

const StyledCard = styled(Card)(({}) => ({
  padding: "16px",
  width: "100%",
}));

const StyledMenuItem = styled(MenuItem)(({}) => ({
  "&.MuiMenuItem-root": {
    color: "black",
    opacity: 1,
  },
  "&.Mui-disabled": {
    color: "black",
    opacity: 1,
  },
  "&.Mui-selected": {
    backgroundColor: "white",
    "&.Mui-focusVisible": {
      background: "white",
    },
  },
}));

export default CapabilityDrawerForm;
