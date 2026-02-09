import { GridColDef } from "@mui/x-data-grid";
import { dataGridTemplate } from "@/components/shared/datagrid/DataGridTemplate";
import { Box } from "@mui/system";
import Subheading from "@/components/shared/display/typography/Subheading";
import React from "react";
import DataGrid from "@/components/shared/datagrid/DataGrid";
import { Divider, Typography } from "@mui/material";

const CheatsheetGrid: GridColDef[] = [
  {
    ...dataGridTemplate,
    field: "operator",
    headerName: "Operator",
    flex: 1,
    renderCell: (params) => (
      <Box
        sx={{
          whiteSpace: 'normal',
          wordBreak: 'break-word',
          py: 2
        }}
      >
        {params.value}
      </Box>
    )
  },
  {
    ...dataGridTemplate,
    field: "selector",
    headerName: "Example selector",
    flex: 2,
    renderCell: (params) => {
      const selector = Array.isArray(params.value) ? params.value : [];

      return (
        <Box
          sx={{
            whiteSpace: 'normal',
            wordBreak: 'break-word',
            mt: 1.5,
          }}
        >
        <ul style={{ margin: 0, paddingLeft: 20 }}>
          {selector.map((point, index) => (
            <li key={index}>{point}</li>
          ))}
        </ul>
        </Box>
      );
    }
  },
  {
    ...dataGridTemplate,
    field: "description",
    headerName: "Description",
    flex: 2,
    renderCell: (params) => (
      <Box
        sx={{
          whiteSpace: 'normal',
          wordBreak: 'break-word',
          mt: 1.5,
        }}
      >
        {params.value}
      </Box>
    )
  }
];

const cheatsheetContent = [
  {
    "id": 1,
    "operator": "IS NULL, IS NOT NULL",
    "description": "Checks if value is null/ not null",
    "selector": ["causeCode IS NULL, quadtree IS NOT NULL"],
  },
  {
    "id": 2,
    "operator": "=, <>, <, <=, >, >=",
    "description": "Comparison operators",
    "selector": ["shardId >= 6"],
  },
  {
    "id": 3,
    "operator": "LIKE",
    "description": "Comparison operators. Wildcards: '%' stands for any sequence of characters, including an empty sequence." +
      " '_' matches a single character",
    "selector": ["(quadTree like '%,12002031%' OR quadTree like '%,1200201%' AND quadTree like '%,10223310%')"],
  },
  {
    "id": 4,
    "operator": "AND, OR, NOT",
    "description": "Combines logical statements",
    "selector": ["NOT (messageType = 'DENM') AND ((causeCode = 1) OR (causeCode = 2)) AND (originatingCountry = 'NO')"]
  }
];

export const Cheatsheet = () => {
  return (
    <Box flex={1}>
      <Subheading>Selector cheatsheet</Subheading>
      <Typography>Need assistance with writing your own selector? Check below for inspiration.</Typography>
      <Divider sx={{ marginY: 2 }} />
      <DataGrid
        columns={CheatsheetGrid}
        rows={cheatsheetContent || []}
        hideFooterPagination={true}
        sort={{ field: "operator", sort: "asc" }}
        getRowHeight={() => 'auto'}
      />
    </Box>
  );
}