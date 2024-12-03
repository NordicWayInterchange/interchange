import React, { useState } from 'react';
import {
    Drawer,
    List,
    ListItem,
    ListItemText,
    ListItemIcon,
    Typography,
    Box,
    CssBaseline,
} from '@mui/material';
import SettingsIcon from '@mui/icons-material/Settings';
import OtherHousesIcon from '@mui/icons-material/OtherHouses';
import Header from './Header';
import Neighbours from "@/components/neighbours/Neighbours";

const drawerWidth = 100;

const MiniVariantDrawer: React.FC = () => {
    const [selectedContent, setSelectedContent] = useState('My interchange');

    const menuItems = [
        { text: 'My interchange', icon: <SettingsIcon />, contentKey: 'My interchange' },
        { text: 'Neighbours', icon: <OtherHousesIcon />, contentKey: 'Neighbours' },
    ];

    const renderContent = () => {
        switch (selectedContent) {
            case 'My interchange':
                return <Typography variant="h4">My interchange</Typography>;
            case 'Neighbours':
                return <Neighbours/>;
            default:
                return <Typography variant="h4">Select an option from the menu</Typography>;
        }
    };

    return (
        <Box sx={{ display: 'flex' }}>
            <CssBaseline />

            <Header title="Admin ui logo" />

            <Drawer
                variant="permanent"
                sx={{
                    width: drawerWidth,
                    flexShrink: 0,
                    '& .MuiDrawer-paper': {
                        width: drawerWidth,
                        boxSizing: 'border-box',
                        backgroundColor: '#202123',
                        display: 'flex',
                        flexDirection: 'column',
                        alignItems: 'center',
                        color: '#A9B7C6',
                        paddingTop: '70px',
                    },
                }}
            >
                <List sx={{ width: '100%' }}>
                    {menuItems.map((item, index) => (
                        <ListItem
                            button
                            key={index}
                            onClick={() => setSelectedContent(item.contentKey)}
                            selected={selectedContent === item.contentKey}
                            sx={{
                                display: 'flex',
                                flexDirection: 'column',
                                alignItems: 'center',
                                justifyContent: 'center',
                                padding: '10px 0',
                                '&:hover': {
                                    backgroundColor: '#3E3F41',
                                },
                                '&.Mui-selected': {
                                    backgroundColor: '#3E3F41',
                                },
                            }}
                        >
                            <ListItemIcon sx={{ color: '#A9B7C6', justifyContent: 'center' }}>
                                {item.icon}
                            </ListItemIcon>
                            <ListItemText
                                primary={item.text}
                                sx={{
                                    textAlign: 'center',
                                    fontSize: '8px',
                                    marginTop: 1,
                                    color: '#A9B7C6',
                                }}
                            />
                        </ListItem>
                    ))}
                </List>
            </Drawer>

            <Box
                component="main"
                sx={{
                    flexGrow: 1,
                    p: 3,
                    backgroundColor: '#2B2B2B',
                    minHeight: '100vh',
                    color: '#A9B7C6',
                }}
            >
                <Box sx={{ height: '64px' }} />
                {renderContent()}
            </Box>
        </Box>
    );
};

export default MiniVariantDrawer;
