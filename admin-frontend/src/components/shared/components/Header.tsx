import React from 'react';
import {AppBar, Box, IconButton, Toolbar, Typography} from '@mui/material';
import LogoutIcon from '@mui/icons-material/Logout';
import {signOut, useSession} from "next-auth/react";
import PersonOutlineIcon from '@mui/icons-material/PersonOutline';
import {styled} from "@mui/system";

const Header = () => {
    const { data: session } = useSession();
    const showLogoutIcon = !!session?.user?.name;

    return (
        <AppBar
            position="fixed"
            sx={{
                zIndex: (theme) => theme.zIndex.drawer + 1,
                backgroundColor: 'headerBackgroundColor',
                color: 'textColor',
            }}
        >
            <Toolbar>
            <Typography variant="h6" noWrap component="div" sx={{ marginRight: "auto" }}>
               Admin UI
            </Typography>
                {showLogoutIcon && (
                    <StyledSignOutBox>
                        <IconButton sx={{ marginRight: "-5px", mb: .25, cursor: 'default'}}>
                            <PersonOutlineIcon sx={{ color: "white", fontSize: "large"}} />
                        </IconButton>
                        <Typography>{session?.user?.name}</Typography>

                        <Box mx={1.5} />
                        <IconButton sx={{ marginRight: "-5px", mb: .25}} onClick={() => signOut()}>
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
};

const StyledSignOutBox = styled(Box)(({}) => ({
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center"
}));

export default Header;
