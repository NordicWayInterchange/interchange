import { Typography } from "@mui/material";
import React, { ReactNode } from "react";
import { TypographyProps } from "@mui/system";

interface Props extends TypographyProps {
  children: ReactNode;
}

const Subheading = ({ children }: Props) => {
  return (
    <Typography variant="subtitle1" fontSize={20}>
      {children}
    </Typography>
  );
};

export default Subheading;
