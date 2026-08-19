import { Typography } from "@mui/material";
import React, { ReactNode } from "react";
import { TypographyProps } from "@mui/system";

interface Props extends TypographyProps {
    children: ReactNode;
}

const Mainheading = ({ children }: Props) => {
    return (
        <Typography variant="h4">
            {children}
        </Typography>
    );
};

export default Mainheading;
