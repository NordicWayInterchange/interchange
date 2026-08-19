import { Box } from "@mui/system";
import { IconButton, Tooltip, Typography } from "@mui/material";
import React, { useEffect, useState } from "react";
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import { tooltipFontStyle } from "@/components/shared/styles/TooltipFontStyle";
import InfoOutlinedIcon from "@mui/icons-material/InfoOutlined";
import LockOutlinedIcon from "@mui/icons-material/LockOutlined";
import { StyledButton } from "@/components/shared/styles/StyledSelectorBuilder";
import { frontPageCardStyle } from "@/components/shared/styles/CardStyle";
import { useSession } from "next-auth/react";
import { useAccessToBiQueue } from "@/hooks/useAccessToBiQueue";
import Loading from "@/components/shared/actions/Loading";
import { addBiqueueAccess } from "@/lib/fetchers/internalFetchers";
import { IFeedback } from "@/interface/IFeedback";
import Snackbar from "@/components/shared/feedback/Snackbar";
import { Stack } from "@mui/material";

const BiConsumer = () => {
  const { data: session } = useSession();
  const { data: biQueueAccess, isLoading } = useAccessToBiQueue(
    session?.user?.commonName as string,
  );

  const [hasAccess, setHasAccess] = useState(false);
  const [open, setOpen] = React.useState(false);

  const [feedback, setFeedback] = useState<IFeedback>({
    feedback: false,
    message: "",
    severity: "success",
  });

  useEffect(() => {
    if (biQueueAccess?.access !== undefined) {
      setHasAccess(biQueueAccess.access ?? false);
    }
  }, [biQueueAccess]);

  const handleSnackClose = (
    _event?: React.SyntheticEvent | Event,
    reason?: string,
  ) => {
    if (reason === "clickaway") {
      return;
    }

    setFeedback({ feedback: false, message: "", severity: "success" });
  };

  const handleOpen = (value: boolean) => () => setOpen(value);

  const handleToggleAccess = async () => {
    const response = await addBiqueueAccess(
      session?.user.commonName as string,
      { access: !hasAccess },
    );

    if (response.ok) {
      setFeedback({
        feedback: true,
        message: `Bi-queue access successfully ${hasAccess ? "revoked" : "granted"}!`,
        severity: "success",
      });
    } else {
      const errorData = await response.json();
      const errorMessage =
        errorData.message || "Bi-queue access could not be granted, try again!";

      setFeedback({
        feedback: true,
        message: errorMessage,
        severity: "warning",
      });
    }
    const result = await response.json();

    if (typeof result.access === "boolean") {
      setHasAccess(result.access);
    }
  };

  return (
    <>
      <Box
        sx={{
          ...frontPageCardStyle,
          justifyContent: "left",
          "@media (min-width:600px)": {
            flexDirection: "row",
            maxWidth: "950px",
          },
          maxWidth: "950px",
        }}
      >
        {biQueueAccess === undefined || isLoading ? (
          <Loading text="Bi-queue access status" />
        ) : (
          <Box>
            <Box
                sx={{
                  display: "flex",
                  flexDirection: "row",
                  alignItems: "center",
                  gap: 1,
                  flexWrap: "wrap",
                  rowGap: 1,
                }}
            >
              {hasAccess ? (
                  <Box
                      onClick={() => handleOpen(true)}
                      sx={{
                        display: "flex",
                        flexDirection: "row",
                        alignItems: "center",
                        gap: 1,
                        cursor: "pointer",
                      }}
                  >
                  <IconButton size="small">
                    <CheckCircleIcon color="success"/>
                  </IconButton>
                  <Typography
                    variant="body2"
                    sx={{ display: "flex", alignItems: "center" }}
                  >
                    I currently have permission to bi-queue.
                    <Tooltip
                        slotProps={{
                          tooltip: {
                            sx: tooltipFontStyle,
                          },
                        }}
                        title="A Bi-queue provides an unfiltered view of all data for a message type produced on a single interchange. A Bi-queue
allows service providers to connect and receive messages over the Basic Interface. You can add or remove access to subscribe to bi-queues"
                    >
                      <IconButton size="small">
                        <InfoOutlinedIcon fontSize="small" sx={{mt: -2}}/>
                      </IconButton>
                    </Tooltip>
                  </Typography>
                </Box>
              ) : (
                  <Box
                      onClick={() => handleOpen(true)}
                      sx={{
                        display: "flex",
                        flexDirection: "row",
                        alignItems: "flex-start",
                        gap: 1,
                        cursor: "pointer",
                      }}
                  >
                  <IconButton size="small" sx={{mt: -1}}>
                    <LockOutlinedIcon color="action" />
                  </IconButton>
                  <Typography
                    variant="body2"
                    sx={{ display: "flex", alignItems: "center" }}
                  >
                    I currently do not have permission to bi-queue.
                    <Tooltip
                        slotProps={{
                          tooltip: {
                            sx: tooltipFontStyle,
                          },
                        }}
                        title="A Bi-queue provides an unfiltered view of all data for a message type produced on a single interchange. A Bi-queue
allows service providers to connect and receive messages over the Basic Interface. You can add or remove access to subscribe to bi-queues"
                    >
                      <IconButton size="small">
                        <InfoOutlinedIcon fontSize="small" sx={{mt: -2}}/>
                      </IconButton>
                    </Tooltip>
                  </Typography>
                  </Box>
              )}

              <StyledButton
                variant="contained"
                color={
                  isLoading
                    ? "grayLight"
                    : hasAccess
                      ? "redLight"
                      : "buttonThemeColor"
                }
                disabled={isLoading}
                onClick={handleToggleAccess}
              >
                {hasAccess ? "Remove my access" : "Give me access"}
              </StyledButton>
            </Box>
          </Box>
        )}
        {feedback.feedback && (
          <Snackbar
            message={feedback.message}
            severity={feedback.severity}
            open={feedback.feedback}
            handleClose={handleSnackClose}
          />
        )}
      </Box>
    </>
  );
};

export default BiConsumer;
