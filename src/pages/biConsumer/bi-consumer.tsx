import { Box, Stack } from "@mui/system";
import { IconButton, Tooltip, Typography } from "@mui/material";
import React, { useEffect, useState } from "react";
import CheckCircleOutlineIcon from "@mui/icons-material/CheckCircleOutline";
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

  const handleClose = () => setOpen(false);

  const handleToggleAccess = async () => {
    const response = await addBiqueueAccess(
      session?.user.commonName as string,
      { access: !hasAccess },
    );

    if (response.ok) {
      setFeedback({
        feedback: true,
        message: `Bi queue access successfully ${hasAccess ? "revoked" : "granted"}!`,
        severity: "success",
      });
    } else {
      const errorData = await response.json();
      const errorMessage =
        errorData.message || "Bi queue access could not be granted, try again!";

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
      <Box sx={frontPageCardStyle}>
        {biQueueAccess === undefined || isLoading ? (
          <Loading text="Bi queue access status" />
        ) : (
          <Box>
            <Stack
              direction="row"
              alignItems="left"
              spacing={2}
              sx={{
                flexWrap: "wrap",
                rowGap: 1,
              }}
            >
              {hasAccess ? (
                <Stack
                  direction="row"
                  alignItems="left"
                  spacing={1}
                  onClick={handleOpen(true)}
                >
                  <IconButton size="small">
                    <CheckCircleOutlineIcon color="success" />
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
                      title="Bi queue is an unfiltered queue without any subscriptions. You can add or remove access to the bi-consumer's group"
                    >
                      <IconButton size="small">
                        <InfoOutlinedIcon fontSize="small" sx={{ mt: -2 }} />
                      </IconButton>
                    </Tooltip>
                  </Typography>
                </Stack>
              ) : (
                <Stack direction="row" alignItems="left" spacing={1} onClick={handleOpen(true)}>
                  <IconButton size="small">
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
                      title="Bi queue is an unfiltered queue without any subscriptions. You can add or remove access to the bi-consumer's group"
                    >
                      <IconButton size="small">
                        <InfoOutlinedIcon fontSize="small" sx={{ mt: -2 }} />
                      </IconButton>
                    </Tooltip>
                  </Typography>
                </Stack>
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
            </Stack>
          </Box>
        )}
      </Box>

    </>
  );
};

export default BiConsumer;
