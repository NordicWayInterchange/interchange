import { Chip, IconButton, InputAdornment, OutlinedInput, TextField, Tooltip, Typography } from "@mui/material";
import React, { useState } from "react";
import { Controller, SubmitHandler, useForm } from "react-hook-form";
import { Box } from "@mui/system";
import Snackbar from "@/components/shared/feedback/Snackbar";
import { IFeedback } from "@/interface/IFeedback";
import { useSession } from "next-auth/react";
import { useRouter } from "next/router";
import { StyledButton, StyledCard, StyledFormControl } from "@/components/shared/styles/StyledSelectorBuilder";
import { IFormPrivateChannelInput } from "@/interface/IFormPrivateChanelInput";
import { createPrivateChannel } from "@/lib/fetchers/internalFetchers";
import CancelIcon from "@mui/icons-material/Cancel";
import { escapeString } from "@/lib/escapeString";
import InfoOutlinedIcon from "@mui/icons-material/InfoOutlined";
import { tooltipFontStyle } from "@/components/shared/styles/TooltipFontStyle";

const PrivateChannelsCreator = () => {
  const [feedback, setFeedback] = useState<IFeedback>({
    feedback: false,
    message: "",
    severity: "success"
  });
  const { data: session } = useSession();
  const router = useRouter();
  const [inputValue, setInputValue] = useState<string>("");

  const {
    handleSubmit,
    control,
    setValue,
    register,
    getValues,
    reset,
    formState: { errors }
  } = useForm<IFormPrivateChannelInput>({
    defaultValues: {
      peers: [],
      description: ""
    }
  });

  const addChip = () => {
    const trimmedValue = escapeString(inputValue.trim());
    if (trimmedValue && !getValues("peers").includes(trimmedValue)) {
      setValue("peers", [...getValues("peers"), trimmedValue], { shouldValidate: true });
      setInputValue("");
    }
  };

  const handleKeyDown = (event: React.KeyboardEvent<HTMLInputElement>) => {
    if (event.key === "Enter" || event.key === "," || event.key === " ") {
      event.preventDefault();
      addChip();
    }
  };

  const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const input = event.target.value;

    if (input.includes(",")) {
      const parts = input.split(",").map((part) => part.trim());
      const lastPart = parts.pop() || "";

      parts.forEach((part) => {
        if (part && !getValues("peers").includes(part)) {
          setValue("peers", [...getValues("peers"), part], { shouldValidate: true });
        }
      });

      setInputValue(lastPart);
    } else {
      setInputValue(input);
    }
  };

  const handleDeleteChip = (valueToDelete: string) => {
    setValue("peers", getValues("peers").filter((chip) => chip !== valueToDelete), { shouldValidate: true });
  };

  const clearForm = () => {
    reset({ peers: [] });
    setInputValue("");
  };

  const onSubmit: SubmitHandler<IFormPrivateChannelInput> = async ({ peers, ...rest }) => {
    const peersWithoutWhitespace = peers.map(item => item.trim()).filter(item => item !== "");

    const response = await createPrivateChannel(
      session?.user.commonName as string,
      { ...rest, peers: peersWithoutWhitespace }
    );

    if (response.ok) {
      setFeedback({
        feedback: true,
        message: "Private channel successfully created",
        severity: "success"
      });
      await router.push("/private-channels");
    } else {
      const errorData = await response.json();
      const errorMessage = errorData.message || "Private channel could not be created, try again!";

      setFeedback({
        feedback: true,
        message: errorMessage,
        severity: "warning"
      });
    }
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
      <StyledCard variant={"outlined"}>
        <form onSubmit={handleSubmit(onSubmit)}>
          <StyledFormControl>
            <Box
              display="flex"
              alignItems="flex-start"
              p={1}
              borderRadius={1}
              border={1}
              flexWrap="wrap"
              sx={{ borderColor: errors.peers ? "red" : "grey.300" }}
            >
              <Typography
                color="textSecondary"
                sx={{
                  marginRight: "8px",
                  position: "relative",
                  top: "3px",
                }}
              >
                Peers *
              </Typography>

              <Box
                display="flex"
                flexWrap="wrap"
                sx={{ gap: "8px", maxWidth: "100%" }}
              >
                {getValues("peers").map((value, index) => (
                  <Chip
                    key={index}
                    label={value}
                    onDelete={() => handleDeleteChip(value)}
                    sx={{
                      backgroundColor: "sidebarActiveColor",
                      border: "1px solid",
                      borderColor: "sidebarBorderColor",
                    }}
                    deleteIcon={<CancelIcon style={{ color: "grey" }} />}
                    variant="outlined"
                  />
                ))}
              </Box>

              <TextField
                variant="standard"
                value={inputValue}
                onChange={handleChange}
                onKeyDown={handleKeyDown}
                multiline
                minRows={1}
                slots={{
                  input: OutlinedInput,
                }}
                slotProps={{
                  input: {
                    endAdornment: (
                      <InputAdornment position="end">
                        <Tooltip
                          title="Use a comma, space, or press enter to separate peers—otherwise, they won't be added properly."
                          placement="top"
                          slotProps={{
                            tooltip: {
                              sx: tooltipFontStyle,
                            },
                          }}
                        >
                          <IconButton edge="end" size="small">
                            <InfoOutlinedIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                      </InputAdornment>
                    ),
                    inputProps: {
                      placeholder: "Add peers (comma, space, or enter to add)",
                      maxLength: 255,
                    },
                  },
                }}
                sx={textFieldStyle}
              />
            </Box>

            <Box sx={{ display: "flex", gap: 1 }}>
              <Controller
                name="description"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    {...register("description", {
                      required: "Description is required",
                    })}
                    fullWidth
                    multiline
                    rows={5}
                    label="Description *"
                    error={!!errors.description}
                    helperText={
                      errors.description ? errors.description.message : ""
                    }
                  />
                )}
              />
            </Box>
            <Box sx={{ display: "flex", justifyContent: "space-evenly" }}>
              <StyledButton
                color="buttonThemeColor"
                variant="contained"
                type="submit"
              >
                Create private channel
              </StyledButton>
              <StyledButton
                color="buttonThemeColor"
                variant="outlined"
                onClick={clearForm}
              >
                Clear form
              </StyledButton>
            </Box>
          </StyledFormControl>
        </form>
      </StyledCard>
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
};

const textFieldStyle = {
  flexGrow: 1,
  marginTop: '10px',
  width: '100%',
  borderRadius: '8px',
  backgroundColor: "#f9f9f9",

  "& .MuiOutlinedInput-root": {
    "&.Mui-focused fieldset": {
      borderColor: "gray",
    },
  },
  padding: '2px 2px'
};

export default PrivateChannelsCreator;

