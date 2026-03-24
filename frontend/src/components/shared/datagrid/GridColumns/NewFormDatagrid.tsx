import { GridColDef } from "@mui/x-data-grid";
import { dataGridTemplate } from "@/components/shared/datagrid/DataGridTemplate";
import { messageTypeChips } from "@/lib/statusChips";
import React from "react";
import { Chip } from "@/components/shared/display/Chip";

export const NewFormDataGrid: GridColDef[] = [
  {
    ...dataGridTemplate,
    field: "publisherId",
    headerName: "Publisher ID",
  },
  {
    ...dataGridTemplate,
    field: "publicationId",
    headerName: "Publication ID",
    flex: 2
  },
  {
    ...dataGridTemplate,
    field: "messageType",
    headerName: "Message type",
    flex: 2,
    renderCell: (cell) => {
      return (
        <Chip
          color={
            messageTypeChips[cell.value as keyof typeof messageTypeChips] as any
          }
          label={cell.value}
        />
      );
    },
  },
  {
    ...dataGridTemplate,
    field: "protocolVersion",
    headerName: "Protocol version",
    flex: 2
  },
  {
    ...dataGridTemplate,
    field: "originatingCountry",
    headerName: "Originating country",
  },
  {
    ...dataGridTemplate,
    field: "shardCount",
    headerName: "Shard count",
  },
];
