import { Box, Stack } from "@mui/system";
import BiConsumer from "@/pages/biConsumer/bi-consumer";
import Subheading from "@/components/shared/display/typography/Subheading";
import { Divider, IconButton } from "@mui/material";
import React, { useState } from "react";
import Mainheading from "@/components/shared/display/typography/Mainheading";
import { dataGridTemplate } from "@/components/shared/datagrid/DataGridTemplate";
import { GridColDef } from "@mui/x-data-grid";
import { useBiQueueEndpoints } from "@/hooks/useBiQueueEndpoints";
import { CustomFooter } from "@/components/shared/datagrid/CustomFooter";
import { CustomEmptyOverlayBiqueueEndpoints } from "@/components/shared/datagrid/CustomEmptyOverlay";
import DataGrid from "@/components/shared/datagrid/DataGrid";
import { BiQueueEndpointsApi } from "@/types/napcore/biQueueResponse";
import BiQueueEndpointDrawer from "@/components/biQueue/BiQueueEndpointDrawer";
import MoreVertIcon from "@mui/icons-material/MoreVert";
import { Chip } from "@/components/shared/display/Chip";
import { messageTypeChips } from "@/lib/statusChips";

export default function BiQueues() {
  const { data, isLoading } = useBiQueueEndpoints();
  const [biqueueEndpointRow, setBiqueueEndpointRow] =
    useState<BiQueueEndpointsApi>();
  const [drawerOpen, setDrawerOpen] = useState<boolean>(false);

  const rows = Array.isArray(data) ? data : [];

  const handleMore = (biQueueEndpoint: BiQueueEndpointsApi) => {
    setBiqueueEndpointRow(biQueueEndpoint);
    setDrawerOpen(true);
  };

  const handleOnRowClick = (params: any) => {
    handleMore(params.row);
  };

  const handleMoreClose = () => {
    setDrawerOpen(false);
  };

  const tableHeaders: GridColDef[] = [
    {
      ...dataGridTemplate,
      field: "messageType",
      headerName: "Message type",
      flex: 2,
      renderCell: (cell) => {
        return (
          <Chip
            color={
              messageTypeChips[
                cell.value as keyof typeof messageTypeChips
              ] as any
            }
            label={cell.value}
          />
        );
      },
    },
    {
      ...dataGridTemplate,
      field: "actions",
      headerName: "",
      sortable: false,
      filterable: false,
      disableColumnMenu: true,
      align: "right",
      renderCell: (params) => {
        return (
          <Box>
            <IconButton onClick={() => handleMore(params.row)}>
              <MoreVertIcon />
            </IconButton>
          </Box>
        );
      },
    },
  ];
  return (
    <Box flex={1}>
      <Mainheading>Bi-queues</Mainheading>
      <Subheading>
        These are all of bi-queues per message type. You can click each row to
        see details.
      </Subheading>
      <Divider sx={{ marginY: 2 }} />
      <Divider style={{ margin: "5px 0", visibility: "hidden" }} />

      <Box display="flex" flexWrap="wrap" gap={3}>
        <Box
          flex={1}
          sx={{
            width: { xs: "100%", sm: "100%", md: "100%", lg: "50%", xl: "50%" },
          }}
        >
          <Subheading>My bi-queue access</Subheading>
          <Divider style={{ margin: "5px 0", visibility: "hidden" }} />
          <BiConsumer></BiConsumer>
        </Box>
        <Box
          flex={1}
          sx={{
            width: { xs: "100%", sm: "100%", md: "100%", lg: "50%", xl: "50%" },
          }}
        >          <Subheading>My bi-queues list</Subheading>
          <Divider style={{ margin: "5px 0", visibility: "hidden" }} />
            <DataGrid
              columns={tableHeaders}
              rows={rows || []}
              onRowClick={handleOnRowClick}
              loading={isLoading}
              getRowId={(row) => `${row.name}-${row.messageType}`}
              sort={{ field: "row?.id", sort: "desc" }}
              slots={{
                footer: CustomFooter,
                noRowsOverlay: CustomEmptyOverlayBiqueueEndpoints,
              }}
            />
          </Box>
      </Box>
      {biqueueEndpointRow?.biqueueEndpointResponse && (
        <BiQueueEndpointDrawer
          open={drawerOpen}
          onClose={handleMoreClose}
          biqueueEndpointRow={biqueueEndpointRow}
        />
      )}
    </Box>
  );
}
