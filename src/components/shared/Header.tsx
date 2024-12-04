import React from 'react';
import { AppBar, Toolbar, Typography } from '@mui/material';
import Subheading from "@/components/shared/typography/Subheading";

interface HeaderProps {
    title: string;
}

const Header: React.FC<HeaderProps> = ({ title }) => {
    return (
        <AppBar
            position="fixed"
            sx={{
                zIndex: (theme) => theme.zIndex.drawer + 1,
                backgroundColor: '#1E1E1E',
                color: 'textColor',
            }}
        >
            <Toolbar>
                <Subheading>
                    {title}
                </Subheading>
            </Toolbar>
        </AppBar>
    );
};

export default Header;
