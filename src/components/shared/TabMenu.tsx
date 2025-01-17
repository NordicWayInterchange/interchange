import React, { useState } from 'react';
import Tabs from '@mui/material/Tabs';
import Tab from '@mui/material/Tab';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';

function TabMenu() {
    const [activeTab, setActiveTab] = useState(0);

    const handleTabChange = (event, newValue) => {
        setActiveTab(newValue);
    };

    return (
        <Box sx={{ width: '100%' }}>
            <Tabs
                value={activeTab}
                onChange={handleTabChange}
                centered
            >
                <Tab label="Subscriptions" />
                <Tab label="Capabilities" />
                <Tab label="Deliveries" />
            </Tabs>

            <Box sx={{ p: 2 }}>
                {activeTab === 0 && <Typography>Subscriptions</Typography>}
                {activeTab === 1 && <Typography>Capabilities</Typography>}
                {activeTab === 2 && <Typography>Deliveries</Typography>}
            </Box>
        </Box>
    );
}

export default TabMenu;
