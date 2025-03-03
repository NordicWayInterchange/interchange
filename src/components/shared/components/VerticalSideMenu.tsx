import React from 'react';
import {
    Drawer,
    List,
    ListItem,
    ListItemText,
    ListItemIcon,
    Box,
    CssBaseline,
} from '@mui/material';
import SyncAltIcon from '@mui/icons-material/SyncAlt';
import Groups2Icon from '@mui/icons-material/Groups2';
import Header from './Header';
import {IPages} from "@/interfaces/IPages";
import Link from "next/link";
import { useRouter } from "next/router";
import HouseIcon from "@mui/icons-material/House";

const drawerWidth = 145;

const SIDE_PAGES: Array<IPages> = [
    {
        text: "Home",
        url: "/",
        icon: <HouseIcon />
    },
    {
        text: "Service Providers",
        url: "/serviceProviders",
        icon: <SyncAltIcon />
    },
    {
        text: "Neighbours",
        url: "/neighbours",
        icon: <Groups2Icon />
    },
];

const VerticalSideMenu: React.FC = () => {
    const router = useRouter();

    const mapPages = (pages: Array<IPages>) => {
        return pages.map((page: IPages, key: number) => (
            <Link
                href={page.url}
                key={key}
                style={{
                    textDecoration: "none",
                    color: "inherit",
                }}
            >
                <ListItem
                    sx={{
                        display: 'flex',
                        flexDirection: 'column',
                        alignItems: 'center',
                        justifyContent: 'center',
                        borderRadius: 1,
                        my: 2,
                        padding: '10px 0',
                        backgroundColor:
                            router.asPath === page.url ? "menuHoverColor" : null,
                        border: "1px solid transparent",
                        '&:hover': {
                            backgroundColor: 'menuHoverColor',
                            border: "1px solid",
                            borderColor: "menuHoverColor",
                        },
                    }}
                    disablePadding
                >

                <ListItemIcon sx={{ justifyContent: 'center', marginTop: 1}}>{page.icon}</ListItemIcon>
                <ListItemText  sx={{
                    textAlign: 'center',
                    marginTop: 1,
                }} primary={page.text} />
                </ListItem>
            </Link>
        ));
    };


    return (
        <Box sx={{ display: 'flex' }}>
            <CssBaseline />
            <Header />
            <Drawer
                variant="permanent"
                sx={{
                    width: drawerWidth,
                    flexShrink: 0,
                    '& .MuiDrawer-paper': {
                        width: drawerWidth,
                        boxSizing: 'border-box',
                        display: 'flex',
                        flexDirection: 'column',
                        alignItems: 'center',
                        paddingTop: '70px',
                    },
                }}
            >
                <Box sx={{ padding: 2 }}>
                    <List>{mapPages(SIDE_PAGES)}</List>
                </Box>
            </Drawer>

            <Box
                component="main"
                sx={{
                    flexGrow: 1,
                    backgroundColor: 'mainBackgroundColor',
                    minHeight: '100vh',
                }}
            >
                <Box sx={{ height: '64px' }} />
            </Box>
        </Box>
    );
};

export default VerticalSideMenu;
