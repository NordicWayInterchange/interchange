import React, { useState } from "react";
import { Card, CardContent, Divider, IconButton, Tooltip, Typography } from "@mui/material";
import { useSession } from "next-auth/react";
import { Box, Stack } from "@mui/system";
import Link from "next/link";
import DataGrid from "@/components/shared/datagrid/DataGrid";
import { useSubscriptions } from "@/hooks/useSubscriptions";
import { SubscriptionDatagrid } from "@/components/shared/datagrid/GridColumns/SubscriptionDatagrid";
import { CustomEmptyOverlaySubscription } from "@/components/shared/datagrid/CustomEmptyOverlay";
import Mainheading from "@/components/shared/display/typography/Mainheading";
import Subheading from "@/components/shared/display/typography/Subheading";
import ShortcutCard from "@/components/shared/display/ShortcutCard";
import SubscriptionsIcon from "@mui/icons-material/Subscriptions";
import CellTowerIcon from "@mui/icons-material/CellTower";
import LocalPostOfficeIcon from "@mui/icons-material/LocalPostOffice";
import SettingsIcon from "@mui/icons-material/Settings";
import PersonIcon from "@mui/icons-material/Person";
import LockIcon from "@mui/icons-material/Lock";
import { ContentCopy } from "@/components/shared/actions/ContentCopy";
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';
import CheckCircleOutlineIcon from '@mui/icons-material/CheckCircleOutline';
import { StyledButton } from "@/components/shared/styles/StyledSelectorBuilder";
import { tooltipFontStyle } from "@/components/shared/styles/TooltipFontStyle";
import InfoOutlinedIcon from "@mui/icons-material/InfoOutlined";

export default function Home() {
  const { data: session } = useSession();
  const { data, isLoading } = useSubscriptions(
    session?.user.commonName as string
  );

  const shortcuts = [
    {
      header: "Subscriptions",
      description: "View subscriptions",
      url: "/subscriptions",
      icon: <SubscriptionsIcon />
    },
    {
      header: "Network capabilities",
      description: "View all capabilities",
      url: "network-capabilities",
      icon: <CellTowerIcon />
    },
    {
      header: "My capabilities",
      description: "View my capabilities",
      url: "capabilities",
      icon: <PersonIcon />
    },
    {
      header: "Deliveries",
      description: "View deliveries",
      url: "/deliveries",
      icon: <LocalPostOfficeIcon />
    },
    {
      header: "Private channels",
      description: "View my private channels",
      url: "/private-channels",
      icon: <LockIcon />
    },
    {
      header: "Certificate",
      description: "Generate certificate",
      url: "/certificate",
      icon: <SettingsIcon />
    },
  ];

  const [hasAccess, setHasAccess] = useState(false);


  const handleToggleAccess = () => {
    setHasAccess((prev) => !prev);
  };

  return (
    <Box flex={1}>
      <Mainheading>Welcome, {session?.user?.name}!</Mainheading>
      <Divider sx={{ marginY: 3 }} />

      <Box sx={elementStyle}>
        <Typography
          sx={{ fontSize: "0.85rem", fontWeight: 600}}>
          Your common name:
        </Typography>
        <Typography variant="h6" sx={{ fontSize: "0.85rem", textAlign: "center"}}>
          {session?.user?.commonName}
        </Typography>
        <Box sx={{ mt: -.5 }}><ContentCopy value={session?.user?.commonName.toString() || ""} /></Box>
      </Box>
      <Divider sx={{ marginY: 2, visibility: 'hidden' }} />

      <Box>
        <Card sx={elementStyle}>
          <CardContent>
            {hasAccess ? (
              <>
                <CheckCircleOutlineIcon color="success" sx={{ fontSize: 50, mb: 2 }} />
                <Typography variant="h6" gutterBottom>
                  Bi Queue Access Granted
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
                  You currently have permission to view Bi queue.
                  <Tooltip
                    slotProps={{
                      tooltip: {
                        sx: tooltipFontStyle,
                      },
                    }}
                    title="Revoke access to the bi-consumers group"
                  >
                    <IconButton size="small">
                      <InfoOutlinedIcon fontSize="small" sx={{ mt: -2 }} />
                    </IconButton>
                  </Tooltip>
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
                    title="Gain access to the bi-consumers group"
                  >
                    <IconButton size="small">
                      <InfoOutlinedIcon fontSize="small" sx={{ mt: -2 }} />
                    </IconButton>
                  </Tooltip>
                </Typography>
              </>
            )}

            <Stack spacing={2} sx={{display: "flex", alignItems: "center",textAlign: 'center', justifyContent: "center"}}>
              <StyledButton
                variant="contained"
                color={hasAccess ? 'redLight' : 'buttonThemeColor'}
                onClick={handleToggleAccess}
              >
                {hasAccess ? 'Revoke Access' : 'Grant Access'}
              </StyledButton>

            </Stack>
          </CardContent>
        </Card>
      </Box>


      <Divider sx={{ marginY: 3 }} />
      <Subheading>Shortcuts</Subheading>
      <Box sx={{ display: "flex", flexDirection: "column", gap: 3 }}>
        <Box
          sx={{
            display: "flex",
            flexWrap: "wrap"
          }}
        >
          {shortcuts.map((shortcut, key) => (
            <Link
              key={key}
              href={shortcut.url}
              style={{
                textDecoration: "none",
                marginRight: 15,
                marginTop: 10,
              }}
            >
              <ShortcutCard
                avatar={<Box
                  sx={{
                    backgroundColor: 'white',
                    display: 'inline-flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    width: '50px',
                    height: '50px',
                  }}
                >
                  {React.cloneElement(shortcut.icon, { style: { color: "#444F55", fontSize: "45px" } })}
                </Box>}
                header={shortcut.header}
                description={shortcut.description}
               />
            </Link>
          ))}
        </Box>
        <Subheading>Your latest subscriptions</Subheading>
        <DataGrid
          columns={SubscriptionDatagrid}
          rows={data?.slice(0, 4) || []}
          loading={isLoading}
          hideFooterPagination={true}
          sort={{ field: "lastStatusChange", sort: "desc" }}
          slots={{
            noRowsOverlay: CustomEmptyOverlaySubscription
          }}
        />
      </Box>
    </Box>
  );
}

const elementStyle = {
  display: "flex",
  alignItems: "center",
  textAlign: 'center',
  bgcolor: "white",
  justifyContent: "center",
  flexDirection: "row",
  padding: 1,
  borderRadius: 2,
  borderBottom: "2px solid #dd7100",
  boxShadow: 3,
  maxWidth: "fit-content",
  margin: "left",
  gap: 0.5,
  mt: 1.5,
  wordBreak: "break-word",
  flexShrink: 0,
  "@media (min-width:600px)": {
    flexDirection: "row",
    maxWidth: "fit-content"
  }
};
