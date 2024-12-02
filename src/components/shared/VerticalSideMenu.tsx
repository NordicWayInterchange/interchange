// components/VeticalSideMenu.tsx
import React, { useState } from 'react';
import {
    Drawer,
    List,
    ListItem,
    ListItemIcon,
    ListItemText,
    IconButton,
    AppBar,
    Toolbar,
    Typography,
    Box,
} from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import SettingsIcon from '@mui/icons-material/Settings';
import OtherHousesIcon from '@mui/icons-material/OtherHouses';
import Neighbours from "@/components/neighbours/Neighbours";

const VerticalSideMenu: React.FC = () => {
    const [drawerOpen, setDrawerOpen] = useState(false);
    const [selectedContent, setSelectedContent] = useState('Home');

    const toggleDrawer = (open: boolean) => () => {
        setDrawerOpen(open);
    };

    const menuItems = [
        { text: 'My interchange', icon: <SettingsIcon />, contentKey: 'My interchange' },
        { text: 'Neighbours', icon: <OtherHousesIcon />, contentKey: 'Neighbours' },
    ];

    const renderContent = () => {
        switch (selectedContent) {
            case 'Neighbours':
                return <Neighbours />;
            default:
                return <Typography>Select an option from the menu</Typography>;
        }
    };

    return (
        <Box sx={{ display: 'flex' }}>
            <AppBar position="fixed">
                <Toolbar>
                    <IconButton
                        edge="start"
                        color="inherit"
                        aria-label="menu"
                        onClick={toggleDrawer(true)}
                        sx={{ marginRight: 2 }}
                    >
                        <MenuIcon />
                    </IconButton>
                    <Typography variant="h6" noWrap>
                    </Typography>
                </Toolbar>
            </AppBar>

            <Drawer anchor="left" open={drawerOpen} onClose={toggleDrawer(false)}>
                <Box
                    sx={{ width: 250 }}
                    role="presentation"
                    onClick={toggleDrawer(false)}
                    onKeyDown={toggleDrawer(false)}
                >
                    <List>
                        {menuItems.map((item, index) => (
                            <ListItem button key={index} onClick={() => setSelectedContent(item.contentKey)}>
                                <ListItemIcon>{item.icon}</ListItemIcon>
                                <ListItemText primary={item.text} />
                            </ListItem>
                        ))}
                    </List>
                </Box>
            </Drawer>

            <Box component="main" sx={{ flexGrow: 1, p: 3, marginTop: '64px' }}>
                {renderContent()}
            </Box>
        </Box>
    );
};

export default VerticalSideMenu;