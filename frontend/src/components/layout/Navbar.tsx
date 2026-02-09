import { AppBar, IconButton, Toolbar, Typography } from "@mui/material";
import React from "react";
import { styled } from "@mui/material/styles";
import PersonOutlineIcon from '@mui/icons-material/PersonOutline';
import { signOut, useSession } from "next-auth/react";
import { Box } from "@mui/system";
import LogoutIcon from '@mui/icons-material/Logout';

export default function Navbar() {
  const { data: session } = useSession();
  const showLogoutIcon = !!session?.user?.name;

  return (
    <AppBar
      elevation={0}
      sx={{
        zIndex: (index) => index.zIndex.drawer + 1,
        bgcolor: "navbarBackgroundColor",
      }}
      position="fixed"
    >
      <Toolbar>
        <Typography variant="h6" noWrap component="div" sx={{ marginRight: "auto" }}>
          {process.env.NEXT_PUBLIC_THEME_PROVIDER == "trafficdata"
            ? "Trafficdata"
            : "Transportportal"}
        </Typography>
        {showLogoutIcon && (
          <StyledSignOutBox>
            <IconButton sx={{ marginRight: "-5px", mb: .25, cursor: 'default'}}>
              <PersonOutlineIcon sx={{ color: "white", fontSize: "large"}} />
            </IconButton>
            <Typography>{session?.user?.name}</Typography>

            <Box mx={1.5} />
            <IconButton sx={{ marginRight: "-5px", mb: .25 }} onClick={() => signOut()}>
              <LogoutIcon sx={{ color: "white", fontSize: "large" }} />
            </IconButton>
            <Typography sx={{
              cursor: "pointer", textDecoration: "none",
              "&:hover": {
                textDecoration: "underline"
              }
            }} onClick={() => signOut()}>Logout</Typography>
          </StyledSignOutBox>
        )}
      </Toolbar>
    </AppBar>
  );
}
const StyledSignOutBox = styled(Box)(({}) => ({
  display: "flex",
  justifyContent: "space-between",
  alignItems: "center"
}));
