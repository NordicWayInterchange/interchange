import { Box } from "@mui/system";
import BiQueue from "@/pages/biQueue/bi-queue";
import Subheading from "@/components/shared/display/typography/Subheading";
import { Divider } from "@mui/material";
import React from "react";
import Mainheading from "@/components/shared/display/typography/Mainheading";
import { dataGridTemplate } from "@/components/shared/datagrid/DataGridTemplate";
import { GridColDef } from "@mui/x-data-grid";

export default function BiQueues() {

  /*const rows = backendData.map((row, index) => ({
    id: index + 1,
    name: row.messageType,
  }));*/
  const tableHeaders: GridColDef[] = [
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
      field: "messateType",
      headerName: "Messate Type"
    }
  ]
  return (
    <Box flex={1}>
      <Mainheading>Bi-queues</Mainheading>
      <Subheading>
        These are all of bi-queues per message type. You can click each row to see details.
      </Subheading>
      <Divider sx={{ marginY: 2 }} />
      <Divider style={{ margin: '5px 0', visibility: 'hidden' }} />
      <Subheading>My bi-queue access</Subheading>
      <Divider style={{ margin: '5px 0', visibility: 'hidden' }} />
      <BiQueue></BiQueue>
      <Divider style={{ margin: '5px 0', visibility: 'hidden' }} />
      <Subheading>My bi-queues</Subheading>
      <Divider style={{ margin: '5px 0', visibility: 'hidden' }} />


    </Box>
  )

}