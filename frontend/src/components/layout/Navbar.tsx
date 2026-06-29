import {AppBar, IconButton, Toolbar, Typography} from "@mui/material";
import React from "react";
import { styled } from "@mui/material/styles";
import { signOut, useSession } from "next-auth/react";
import LogoutIcon from '@mui/icons-material/Logout';
import Image from "next/image";
import logo from "../../../public/interchange-logo.png";
import {Box} from "@mui/system";
import PersonIcon from '@mui/icons-material/Person';

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
        <Box sx={{ alignSelf: "center", mr: 1, mt:1}}>
          <Image src={logo} alt="Nordic Way logo" width={40} priority={true} />
        </Box>
        <Typography variant="h6" noWrap component="div" sx={{ marginRight: "auto" }}>
          {process.env.NEXT_PUBLIC_THEME_PROVIDER == "trafficdata"
            ? "Trafficdata"
            : "Interchange Portal"}
        </Typography>
        {showLogoutIcon && (
          <StyledSignOutBox>
            <IconButton sx={{ marginRight: "-5px", mb: .25, cursor: 'default'}}>
              <PersonIcon sx={{ color: "white", fontSize: "large"}} />
            </IconButton>
            <Typography>{session?.user?.name}</Typography>

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
