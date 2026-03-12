import React from "react";
import { Chip as MuiChip, ChipProps } from "@mui/material";
import { styled } from "@mui/material/styles";

interface Props extends ChipProps {}
export const Chip = (props: Props) => {
  return <StyledDataGrid {...props} />;
};

const StyledDataGrid = styled(MuiChip)(({}) => ({
  borderRadius: 1,
}));
