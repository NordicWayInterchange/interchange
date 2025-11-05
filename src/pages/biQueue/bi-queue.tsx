import { Box, Stack } from "@mui/system";
import {
  Card,
  CardContent,
  IconButton,
  Tooltip,
  Typography,
} from "@mui/material";
import React, { useState } from "react";
import CheckCircleOutlineIcon from "@mui/icons-material/CheckCircleOutline";
import { tooltipFontStyle } from "@/components/shared/styles/TooltipFontStyle";
import InfoOutlinedIcon from "@mui/icons-material/InfoOutlined";
import LockOutlinedIcon from "@mui/icons-material/LockOutlined";
import { StyledButton } from "@/components/shared/styles/StyledSelectorBuilder";
import { frontPageCardStyle } from "@/components/shared/styles/CardStyle";
import { useSession } from "next-auth/react";
import { useAccessToBiQueue } from "@/hooks/useAccessToBiQueue";
import Loading from "@/components/shared/actions/Loading";

const BiQueue = () => {

  const { data: session } = useSession();
  const { data: biQueueAccess, isLoading} = useAccessToBiQueue(
    session?.user?.commonName as string
  );
  const [hasAccess, setHasAccess] = useState(false);


  const handleToggleAccess = () => {
    setHasAccess((prev) => !prev);
  };

  return (
    <Box>
      <Card sx={frontPageCardStyle}>
        {(biQueueAccess === undefined || biQueueAccess === null || isLoading) ? (
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
                You currently have permission Bi queue.
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
                  title="Bi queue is an unfiltered queue without any subscriptions. You can give access to the bi-consumer's group"
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
    </Box>
  );
};

export default BiQueue;
