import React, {useEffect, useState } from "react";
import { Box, Divider, IconButton } from "@mui/material";
import { useDeliveries } from "@/hooks/useDeliveries";
import { GridColDef } from "@mui/x-data-grid";
import DataGrid from "@/components/shared/datagrid/DataGrid";
import { dataGridTemplate } from "@/components/shared/datagrid/DataGridTemplate";
import { useSession } from "next-auth/react";
import MoreVertIcon from "@mui/icons-material/MoreVert";
import {statusChips } from "@/lib/statusChips";
import { Chip } from "@/components/shared/display/Chip";
import { CustomEmptyOverlayDeliveries } from "@/components/shared/datagrid/CustomEmptyOverlay";
import Mainheading from "@/components/shared/display/typography/Mainheading";
import Subheading from "@/components/shared/display/typography/Subheading";
import { timeConverter } from "@/lib/timeConverter";
import { ExtendedDelivery } from "@/types/delivery";
import DeleteIcon from "@mui/icons-material/Delete";
import DeleteSubDialog from "@/components/shared/actions/DeleteSubDialog";
import CommonDrawer from "@/components/layout/CommonDrawer";
import { CustomFooter } from "@/components/shared/datagrid/CustomFooter";
import AddButton from "@/components/shared/actions/AddButton";
import { performRefetch } from "@/lib/performRefetch";
import SearchBox from "@/components/shared/SearchBox";
import { useQueryClient } from "@tanstack/react-query";

export default function Deliveries() {
  const { data: session } = useSession();
  const { data, isLoading, refetch } = useDeliveries(
    session?.user.commonName as string
  );
  const [open, setOpen] = useState<boolean>(false);
  const [deliveryRow, setDeliveryRow] = useState<ExtendedDelivery>();
  const [drawerOpen, setDrawerOpen] = useState<boolean>(false);
  const [isDeleted, setIsDeleted] = useState<boolean>(false);
  const [shouldRefreshAfterDelete, setShouldRefreshAfterDelete] = useState<boolean>(false);
  const queryClient = useQueryClient();
  const [searchId, setSearchId] = useState("");

  useEffect(() => {
    if (isDeleted) {
      performRefetch(refetch);
      setIsDeleted(false);
      setShouldRefreshAfterDelete(true);
    }
  }, [isDeleted, refetch]);

  useEffect(() => {
    const timeout = setTimeout(() => {
      performRefetch(refetch);
    }, 30000);
    setShouldRefreshAfterDelete(false);
    return () => {
      clearTimeout(timeout);
    }
  }, [shouldRefreshAfterDelete, refetch]);


  const handleDelete = (delivery: ExtendedDelivery, event: React.MouseEvent<HTMLButtonElement, MouseEvent>) => {
    event.stopPropagation();
    setDrawerOpen(false);
    setDeliveryRow(delivery);
    setOpen(true);
  };

  const handleMore = (delivery: ExtendedDelivery) => {
    setDeliveryRow(delivery);
    setDrawerOpen(true);
  };

  const handleMoreClose = () => {
    setDrawerOpen(false);
  };

  const handleClickClose = (close: boolean) => {
    setOpen(close);
    queryClient.removeQueries({ queryKey: ['deliveries'] });
  };

  const handleOnRowClick = (params: any) => {
    handleMore(params.row);
  };

  const handleDeletedItem = (deleted: boolean) => {
    setIsDeleted(deleted);
  };

  const rows = Array.isArray(data) ? data : [];

  const filteredDeliveryRows = searchId.trim()
    ? rows.filter((row) =>
      row.id?.toString().includes(searchId.trim())
    )
    : rows;

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
      field: "lastUpdatedTimestamp",
      headerName: "Last updated",
      renderCell: (params) => {
        const value = params.row.lastUpdatedTimestamp;
        return value && timeConverter(value);
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
            <IconButton onClick={(event) => handleDelete(params.row, event)}>
              <DeleteIcon />
            </IconButton>
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
      <Mainheading>Deliveries</Mainheading>
      <Subheading>
        These are all of the deliveries in the network. You can click a
        delivery to see details or remove.
      </Subheading>
      <Divider sx={{ marginY: 2 }} />
      <Box display="flex" flexDirection="row" gap={5}>
      <AddButton text="Create delivery" labelUrl="delivery"></AddButton>
      <SearchBox searchId={searchId} setSearchId={setSearchId} label="delivery" searchElement="ID"/>
      </Box>
      <Divider style={{ margin: '5px 0', visibility: 'hidden' }}/>
      <DataGrid
        columns={tableHeaders}
        rows={filteredDeliveryRows || []}
        onRowClick={handleOnRowClick}
        loading={isLoading}
        getRowId={(row) => row.id}
        sort={{ field: "lastUpdatedTimestamp", sort: "desc" }}
        slots={{
          footer: CustomFooter,
          noRowsOverlay: CustomEmptyOverlayDeliveries,
        }}
        sx={{
          '& .MuiDataGrid-columnHeaders': {
            borderBottom: '2px solid #1976d2',
          },
        }}
      />
      {deliveryRow && (
        <CommonDrawer
          handleMoreClose={handleMoreClose}
          open={drawerOpen}
          item={deliveryRow as ExtendedDelivery}
          handleDeletedItem={handleDeletedItem}
          label="Delivery"
        />
      )}
      <DeleteSubDialog
        itemId={deliveryRow?.id as string}
        handleDialog={handleClickClose}
        open={open}
        actorCommonName={session?.user.commonName as string}
        handleDeletedItem={handleDeletedItem}
        text="Delivery"
        handleMoreClose={handleMoreClose}
      />
    </Box>
  );
}
