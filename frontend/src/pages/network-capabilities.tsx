import React, { useState } from "react";
import { Box, Divider, IconButton } from "@mui/material";
import { useNetworkCapabilities } from "@/hooks/useNetworkCapabilities";
import { GridColDef } from "@mui/x-data-grid";
import DataGrid from "@/components/shared/datagrid/DataGrid";
import { dataGridTemplate } from "@/components/shared/datagrid/DataGridTemplate";
import { useSession } from "next-auth/react";
import MoreVertIcon from "@mui/icons-material/MoreVert";
import { ExtendedCapability } from "@/types/capability";
import CapabilitiesDrawer from "@/components/capabilities/CapabilitiesDrawer";
import { messageTypeChips } from "@/lib/statusChips";
import { Chip } from "@/components/shared/display/Chip";
import { CustomEmptyOverlayCapabilites } from "@/components/shared/datagrid/CustomEmptyOverlay";
import Mainheading from "@/components/shared/display/typography/Mainheading";
import Subheading from "@/components/shared/display/typography/Subheading";
import SearchBox from "@/components/shared/SearchBox";

export default function NetworkCapabilities() {
  const { data: session } = useSession();
  const { data, isLoading } = useNetworkCapabilities(
    session?.user.commonName as string
  );
  const [capabilityRow, setCapabilityRow] = useState<ExtendedCapability>();
  const [drawerOpen, setDrawerOpen] = useState<boolean>(false);
  const [searchId, setSearchId] = useState("");

  const handleMore = (capability: ExtendedCapability) => {
    setCapabilityRow(capability);
    setDrawerOpen(true);
  };

  const handleMoreClose = () => {
    setDrawerOpen(false);
  };

  const tableHeaders: GridColDef[] = [
    { ...dataGridTemplate, field: "publisherId", headerName: "Publisher ID" },
    {
      ...dataGridTemplate,
      field: "publicationId",
      headerName: "Publication ID",
    },
    {
      ...dataGridTemplate,
      field: "messageType",
      headerName: "Message type",
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
      field: "protocolVersion",
      headerName: "Protocol version",
    },
    {
      ...dataGridTemplate,
      field: "originatingCountry",
      headerName: "Originating country",
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

  const handleOnRowClick = (params: any) => {
    handleMore(params.row);
  };

  const rows = Array.isArray(data) ? data : [];

  const filteredCapabilitiesRows = searchId.trim()
    ? rows.filter((row) =>
      row.publicationId?.toString().includes(searchId.trim())
    )
    : rows;

  return (
    <Box flex={1}>
      <Mainheading>Network Capabilities</Mainheading>
      <Subheading>
        These are all of the capabilites in the network. You can click a
        capability to see details or subscribe.
      </Subheading>
      <Divider sx={{ marginY: 2 }} />
      <SearchBox searchId={searchId} setSearchId={setSearchId} label="capability" searchElement="publicationID"/>
      <Divider style={{ margin: '8px 0', visibility: 'hidden' }}/>
      <DataGrid
        columns={tableHeaders}
        rows={filteredCapabilitiesRows || []}
        onRowClick={handleOnRowClick}
        loading={isLoading}
        getRowId={(row) => row.publicationId}
        sort={{ field: "lastUpdatedTimestamp", sort: "desc" }}
        slots={{
          noRowsOverlay: CustomEmptyOverlayCapabilites,
        }}
      />
      {capabilityRow && (
        <CapabilitiesDrawer
          handleMoreClose={handleMoreClose}
          open={drawerOpen}
          capability={capabilityRow as ExtendedCapability}
        />
      )}
    </Box>
  );
}
