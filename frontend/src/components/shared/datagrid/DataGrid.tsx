import React from "react";
import { DataGrid as MuiDataGrid, DataGridProps } from "@mui/x-data-grid";
import { styled } from "@mui/material/styles";
import { Box } from "@mui/material";
import { GridSortDirection } from "@mui/x-data-grid/models/gridSortModel";

interface Props extends DataGridProps {
  sort: { field: string; sort: GridSortDirection };
}

export default function DataGrid(props: Props) {
  const { sort } = props;
  return (
    <Box sx={{ width: "100%" }}>
      <StyledDataGrid
        {...props}
        autoHeight
        disableRowSelectionOnClick={true}
        sx={{ backgroundColor: "white",
          boxShadow: 1,
          borderRadius: 1
      }}
        initialState={{
          pagination: {
            paginationModel: {
              pageSize: 10,
            },
          },
          sorting: {
            sortModel: [sort],
          },
        }}
        pageSizeOptions={[10, 25, 50]}
      />
    </Box>
  );
}

const StyledDataGrid = styled(MuiDataGrid)(({}) => ({
  "& .MuiDataGrid-cell:focus-within": {
    outline: "none",
  },
  "& .MuiDataGrid-columnHeader:focus-within": {
    outline: "none",
  },
  "& .MuiDataGrid-columnHeader": {
    borderBottom: '2px solid #dd7100 !important',
  },
  "& .MuiDataGrid-columnHeaderTitle": { fontWeight: "bold" },
}));
