import React, { useEffect, useState } from "react";
import { Box, Divider, IconButton } from "@mui/material";
import { GridColDef } from "@mui/x-data-grid";
import DataGrid from "@/components/shared/datagrid/DataGrid";
import { dataGridTemplate } from "@/components/shared/datagrid/DataGridTemplate";
import { useSession } from "next-auth/react";
import MoreVertIcon from "@mui/icons-material/MoreVert";
import { ExtendedCapability } from "@/types/capability";
import { messageTypeChips } from "@/lib/statusChips";
import { Chip } from "@/components/shared/display/Chip";
import { CustomEmptyOverlayUserCapabilites } from "@/components/shared/datagrid/CustomEmptyOverlay";
import Mainheading from "@/components/shared/display/typography/Mainheading";
import Subheading from "@/components/shared/display/typography/Subheading";
import { useUserCapabilities } from "@/hooks/useCapabilities";
import UserCapabilitiesDrawer from "@/components/userCapabilities/UserCapabilitiesDrawer";
import DeleteSubDialog from "@/components/shared/actions/DeleteSubDialog";
import DeleteIcon from "@mui/icons-material/Delete";
import { CustomFooter } from "@/components/shared/datagrid/CustomFooter";
import AddButton from "@/components/shared/actions/AddButton";
import { performRefetch } from "@/lib/performRefetch";
import SearchBox from "@/components/shared/SearchBox";
import { useQueryClient } from "@tanstack/react-query";

export default function Capabilities() {
  const { data: session } = useSession();
  const { data, isLoading, refetch } = useUserCapabilities(
    session?.user.commonName as string
  );

  const [userCapabilityRow, setUserCapabilityRow] = useState<ExtendedCapability>();
  const [drawerOpen, setDrawerOpen] = useState<boolean>(false);
  const [open, setOpen] = useState<boolean>(false);
  const [isDeleted, setIsDeleted] = useState<boolean>(false);
  const queryClient = useQueryClient();
  const [searchId, setSearchId] = useState("");
  const [switchChecked, setSwitchChecked] = useState(false);

  useEffect(() => {
    if (isDeleted) {
      performRefetch(refetch);
      setIsDeleted(false);
    }
  }, [isDeleted, refetch]);

  const handleSwitchChange = (checked: boolean) => {
    setSwitchChecked(checked);
  };

  const handleDelete = (capability: ExtendedCapability, event: React.MouseEvent<HTMLButtonElement, MouseEvent>) => {
    event.stopPropagation();
    setDrawerOpen(false);
    setUserCapabilityRow(capability);
    setOpen(true);
  };

  const handleMore = (capability: ExtendedCapability) => {
    setUserCapabilityRow(capability);
    setDrawerOpen(true);
  };

  const handleMoreClose = () => {
    setDrawerOpen(false);
  };

  const handleClickClose = (close: boolean) => {
    setOpen(close);
    queryClient.removeQueries({ queryKey: ['capabilities'] });
  };

  const handleOnRowClick = (params: any) => {
    handleMore(params.row);
  };

  const handleDeletedItem = (deleted: boolean) => {
    setIsDeleted(deleted);
  };

  const rows = Array.isArray(data) ? data : [];

  const filteredCapabilitiesRows = searchId.trim()
    ? rows.filter((row) =>
      row.publicationId?.toString().includes(searchId.trim())
    )
    : rows;

  const tableHeaders: GridColDef[] = [
    { ...dataGridTemplate, field: "publisherId", headerName: "Publisher ID" },
    {
      ...dataGridTemplate,
      field: "publicationId",
      headerName: "Publication ID"
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
      }
    },
    {
      ...dataGridTemplate,
      field: "protocolVersion",
      headerName: "Protocol version"
    },
    {
      ...dataGridTemplate,
      field: "originatingCountry",
      headerName: "Originating country"
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
            <IconButton onClick={(event) => handleDelete(params.row, event)}>
              <DeleteIcon />
            </IconButton>
            <IconButton onClick={() => handleMore(params.row)}>
              <MoreVertIcon />
            </IconButton>
          </Box>
        );
      }
    }
  ];

  return (
    <Box flex={1}>
      <Mainheading>My Capabilities</Mainheading>
      <Subheading>
        These are all of user capabilities in the network. You can click a capability to see details, remove or deliver.
      </Subheading>
      <Divider sx={{ marginY: 2 }} />
      <Box display="flex" flexDirection="row" gap={5}>
      <AddButton text="Add capability" labelUrl="capability"></AddButton>
      <SearchBox searchId={searchId} setSearchId={setSearchId} label="capability" searchElement="publicationID"/>
      </Box>
      <Divider style={{ margin: '5px 0', visibility: 'hidden' }}/>
      <DataGrid
        columns={tableHeaders}
        rows={filteredCapabilitiesRows || []}
        onRowClick={handleOnRowClick}
        loading={isLoading}
        getRowId={(row) => row.publicationId}
        sort={{ field: "userCapabilityRow?.id", sort: "desc" }}
        slots={{
          footer: CustomFooter,
          noRowsOverlay: CustomEmptyOverlayUserCapabilites
        }}
      />
      {userCapabilityRow && (
        <UserCapabilitiesDrawer
          handleMoreClose={handleMoreClose}
          open={drawerOpen}
          capability={userCapabilityRow as ExtendedCapability}
          handleDeletedItem={handleDeletedItem}
          onSwitchChange={handleSwitchChange}
        />
      )}
      <DeleteSubDialog
        itemId={userCapabilityRow?.id as string}
        handleDialog={handleClickClose}
        open={open}
        actorCommonName={session?.user.commonName as string}
        handleDeletedItem={handleDeletedItem}
        text="Capability"
        handleMoreClose={handleMoreClose}
      />
    </Box>
  );
}