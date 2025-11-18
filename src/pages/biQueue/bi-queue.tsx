import { Box, Stack } from "@mui/system";
import {
  Card,
  CardContent,
  IconButton,
  Tooltip,
  Typography,
} from "@mui/material";
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
import Snackbar from "@/components/shared/feedback/Snackbar";

const BiQueue = () => {

  const { data: session } = useSession();
  const { data: biQueueAccess, isLoading} = useAccessToBiQueue(
    session?.user?.commonName as string
  );
  const [hasAccess, setHasAccess] = useState(false);

  const [feedback, setFeedback] = useState<IFeedback>({
    feedback: false,
    message: "",
    severity: "success"
  });

  useEffect(() => {
    if (biQueueAccess?.access !== undefined) {
        setHasAccess(biQueueAccess.access ?? false);
    }
  }, [biQueueAccess]);

  const handleSnackClose = (
    _event?: React.SyntheticEvent | Event,
    reason?: string
  ) => {
    if (reason === "clickaway") {
      return;
    }

    setFeedback({ feedback: false, message: "", severity: "success" });
  };

  const handleToggleAccess = async () => {
    const response = await addBiqueueAccess(
      session?.user.commonName as string,
      { access: !hasAccess }
    );

    if (response.ok) {
      setFeedback({
        feedback: true,
        message:  `Bi queue access successfully ${hasAccess ? "revoked" : "granted"}!`,
        severity: "success"
      });
    } else {
      const errorData = await response.json();
      const errorMessage = errorData.message || "Bi queue access could not be granted, try again!";

      setFeedback({
        feedback: true,
        message: errorMessage,
        severity: "warning"
      });
    }
    const result = await response.json();

    if (typeof result.access === "boolean") {
      setHasAccess(result.access);
    }
  };

  return (
    <Box>
      <Card sx={frontPageCardStyle}>
        {(biQueueAccess === undefined || isLoading) ? (
          <Loading text="Bi queue access status"/>
        ) : (
        <CardContent>
          {hasAccess ? (
            <>
              <CheckCircleOutlineIcon
                color="success"
                sx={{ fontSize: 50, mb: 2 }}
              />
              <Typography variant="h6" gutterBottom>
                Bi Queue Access Granted
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
                You currently have permission to Bi queue.
              </Typography>
            </>
          ) : (
            <>
              <LockOutlinedIcon color="action" sx={{ fontSize: 50, mb: 2 }} />
              <Typography variant="h6" gutterBottom>
                No Access to Bi Queue
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
                You currently do not have permission to Bi queue.
                <Tooltip
                  slotProps={{
                    tooltip: {
                      sx: tooltipFontStyle,
                    },
                  }}
                  title="Bi queue is an unfiltered queue without any subscriptions. You can allow or revoke access to the bi-consumer's group"
                >
                  <IconButton size="small">
                    <InfoOutlinedIcon fontSize="small" sx={{ mt: -2 }} />
                  </IconButton>
                </Tooltip>
              </Typography>
            </>
          )}

          <Stack
            spacing={2}
            sx={{
              display: "flex",
              alignItems: "center",
              textAlign: "center",
              justifyContent: "center",
            }}
          >
            <StyledButton
              variant="contained"
              color={isLoading ? "grayLight" : hasAccess  ? "redLight" : "buttonThemeColor"}
              disabled={isLoading}
              onClick={handleToggleAccess}
            >
              {hasAccess ? "Revoke Access" : "Grant Access"}
            </StyledButton>
          </Stack>
        </CardContent>
          )}
      </Card>
      {feedback.feedback && (
        <Snackbar
          message={feedback.message}
          severity={feedback.severity}
          open={feedback.feedback}
          handleClose={handleSnackClose}
        />
      )}
    </Box>
  );
};

export default BiQueue;
