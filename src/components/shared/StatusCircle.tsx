import {Box} from "@mui/material";
import React from "react";

const getStatusColor = (status) => {
    switch (status) {
        case 'CONNECTED':
            return '#1D7721';
        case 'FAILED':
            return '#B63434';
        case 'UNREACHABLE':
            return '#9e9e9e';
        default:
            return '#9e9e9e';
    }
};
export const StatusCircle = ({ status }) => {
    const color = getStatusColor(status);
    return (
        <Box
            sx={{
                width: 20,
                height: 20,
                borderRadius: '50%',
                backgroundColor: color,
                display: 'inline-block',
                mb: -.5
            }}
        ></Box>
    );
};