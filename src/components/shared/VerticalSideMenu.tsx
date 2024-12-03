import React, { useState } from 'react';
import {
    Drawer,
    List,
    ListItem,
    ListItemIcon,
    AppBar,
    Toolbar,
    Typography,
    Box, CssBaseline, ListItemText
} from '@mui/material';
import SettingsIcon from '@mui/icons-material/Settings';
import OtherHousesIcon from '@mui/icons-material/OtherHouses';
import Neighbours from "@/components/neighbours/Neighbours";

const drawerWidth = 80; // Fixed width for the drawer

const VerticalSideMenu: React.FC = () => {
    const [selectedContent, setSelectedContent] = useState('Home');

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
            <CssBaseline />

            {/* AppBar */}
            <AppBar position="fixed" sx={{ zIndex: (theme) => theme.zIndex.drawer + 1 }}>
                <Toolbar>
                    <Typography variant="h6" noWrap>
                        Mini Variant Drawer
                    </Typography>
                </Toolbar>
            </AppBar>

            {/* Drawer */}
            <Drawer
                variant="permanent"
                sx={{
                    width: drawerWidth,
                    flexShrink: 0,
                    '& .MuiDrawer-paper': {
                        width: drawerWidth,
                        boxSizing: 'border-box',
                        backgroundColor: '#000', // Black background for the drawer
                        color: '#fff', // White text for contrast in the drawer
                        display: 'flex',
                        flexDirection: 'column', // Stack icons and text vertically
                        alignItems: 'center',
                    },
                }}
            >
                <Toolbar />
                <List sx={{ width: '100%' }}>
                    {menuItems.map((item, index) => (
                        <ListItem
                            button
                            key={index}
                            onClick={() => setSelectedContent(item.contentKey)}
                            sx={{
                                display: 'flex',
                                flexDirection: 'column', // Stack icon and text vertically
                                alignItems: 'center',
                                paddingY: 2,
                            }}
                        >
                            <ListItemIcon sx={{ color: 'white' }}>{item.icon}</ListItemIcon>
                            <ListItemText
                                primary={item.text}
                                sx={{
                                    textAlign: 'center',
                                    fontSize: item.text === 'Settings' ? '10px' : '12px', // Make Settings text smaller
                                    color: 'white',
                                    marginTop: 1,
                                }}
                            />
                        </ListItem>
                    ))}
                </List>
            </Drawer>

            {/* Content Area */}
            <Box component="main" sx={{ flexGrow: 1, p: 3, color: 'black', backgroundColor: '#F7F7F7' }}>
                <Toolbar />
                {renderContent()}
            </Box>
        </Box>
    );
};
export default VerticalSideMenu;