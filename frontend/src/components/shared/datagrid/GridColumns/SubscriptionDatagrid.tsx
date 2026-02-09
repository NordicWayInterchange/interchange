import { GridColDef } from "@mui/x-data-grid";
import { dataGridTemplate } from "@/components/shared/datagrid/DataGridTemplate";
import { Chip } from "@/components/shared/display/Chip";
import { statusChips } from "@/lib/statusChips";
import React from "react";
import { timeConverter } from "@/lib/timeConverter";

export const SubscriptionDatagrid: GridColDef[] = [
  {
    ...dataGridTemplate,
    field: "id",
    headerName: "ID",
    renderCell: (params) => {
      const value = params.row.id;
      return value ? value.substring(0, 8) : '';
    },
  },
  {
    ...dataGridTemplate,
    field: "status",
    headerName: "Status",
    renderCell: (cell) => {
      return (
        <Chip
          color={statusChips[cell.value as keyof typeof statusChips] as any}
          label={cell.value}
        />
      );
    },
  },
  {
    ...dataGridTemplate,
    field: "capabilityMatches",
    headerName: "Capability matches",
  },
  {
    ...dataGridTemplate,
    field: "description",
    headerName: "Description",
    flex: 2
  },
  {
    ...dataGridTemplate,
    field: "lastStatusChange",
    headerName: "Last status change",
    renderCell: (params) => {
      const value = params.row.lastStatusChange;
      return value && timeConverter(value);
    },
  }
];
