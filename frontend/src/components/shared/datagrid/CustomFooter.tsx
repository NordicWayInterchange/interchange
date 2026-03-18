import { Divider } from "@mui/material";
import { GridPagination } from "@mui/x-data-grid";
import React from "react";

export const CustomFooter = () => {
  return (
    <>
      <Divider />
        <GridPagination />
    </>
  );
};
