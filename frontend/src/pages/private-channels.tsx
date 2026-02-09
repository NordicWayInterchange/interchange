import React, { useEffect, useState } from "react";
import { Box, Divider, IconButton} from "@mui/material";
import Mainheading from "@/components/shared/display/typography/Mainheading";
import Subheading from "@/components/shared/display/typography/Subheading";
import { GridColDef } from "@mui/x-data-grid";
import { dataGridTemplate } from "@/components/shared/datagrid/DataGridTemplate";
import { Chip } from "@/components/shared/display/Chip";
import { statusChips } from "@/lib/statusChips";
import { useSession } from "next-auth/react";
import { PrivateChannel, PrivateChannelPeers } from "@/types/napcore/privateChannel";
import { usePrivateChannels } from "@/hooks/usePrivateChannels";
import DataGrid from "@/components/shared/datagrid/DataGrid";
import {
  CustomEmptyOverlayPrivateChannels
} from "@/components/shared/datagrid/CustomEmptyOverlay";
import AddButton from "@/components/shared/actions/AddButton";
import PrivateChannelsDrawer from "@/components/privateChannels/PrivateChannelsDrawer";
import MoreVertIcon from "@mui/icons-material/MoreVert";
import DeleteIcon from "@mui/icons-material/Delete";
import { performRefetch } from "@/lib/performRefetch";
import PeersDrawer from "@/components/privateChannels/PeersDrawer";
import { usePeers } from "@/hooks/usePeers";
import { timeConverter } from "@/lib/timeConverter";
import DeleteSubDialog from "@/components/shared/actions/DeleteSubDialog";
import SearchBox from "@/components/shared/SearchBox";

export default function PrivateChannels() {

  const { data: session } = useSession();
  const { data, isLoading, refetch  } = usePrivateChannels(
    session?.user.commonName as string
  );

  const {
    data: peersData,
    isLoading: isPeersLoading,
    refetch: peerRefetch
  } = usePeers(session?.user.commonName as string);

  const [privateChannelRow, setPrivateChannelRow] = useState<PrivateChannel>();
  const [peersRow, setPeersRow] = useState<PrivateChannelPeers>();
  const [drawerOpen, setDrawerOpen] = useState<boolean>(false);
  const [peersDrawerOpen, setPeersDrawerOpen] = useState<boolean>(false);
  const [isDeleted, setIsDeleted] = useState<boolean>(false);
  const [isPeerDeleted, setIsPeerDeleted] = useState<boolean>(false);
  const [open, setOpen] = useState<boolean>(false);
  const [peerOpen, setPeerOpen] = useState<boolean>(false);
  const [shouldRefreshAfterDelete, setShouldRefreshAfterDelete] = useState<boolean>(false);
  const [shouldRefreshPeersAfterDelete, setShouldRefreshPeersAfterDelete] = useState<boolean>(false);
  const [searchId, setSearchId] = useState("");
  const [peerSearchId, setPeerSearchId] = useState("");

  const hasPeersData =  peersData && peersData.length > 0;

  useEffect(() => {
    const handleDelete = (isDeletedFlag: boolean, refetchFunc: () => void, resetFlag: () => void, setRefreshFlag: (value: boolean) => void) => {
      if (isDeletedFlag) {
        performRefetch(refetchFunc);
        resetFlag();
        setRefreshFlag(true);
      }
    };

    handleDelete(isDeleted, refetch, () => setIsDeleted(false), setShouldRefreshAfterDelete);
    handleDelete(isPeerDeleted, peerRefetch, () => setIsPeerDeleted(false), setShouldRefreshPeersAfterDelete);

  }, [isDeleted, isPeerDeleted, refetch, peerRefetch]);

  useEffect(() => {
    const timeout = setTimeout(() => performRefetch(refetch), 30000);
    const peerTimeout = setTimeout(() => performRefetch(peerRefetch), 30000);

    setShouldRefreshPeersAfterDelete(false);

    return () => {
      clearTimeout(timeout);
      clearTimeout(peerTimeout);
    };
  }, [shouldRefreshAfterDelete, shouldRefreshPeersAfterDelete, refetch, peerRefetch]);

  const handleMore = (privateChannel: PrivateChannel) => {
    setPrivateChannelRow(privateChannel);
    setDrawerOpen(true);
  };

  const handlePeersMore = (peers: PrivateChannelPeers) => {
    setPeersRow(peers);
    setPeersDrawerOpen(true);
  };

  const handleMoreClose = () => {
    setDrawerOpen(false);
  };

  const handleClickClose = (close: boolean) => {
    setOpen(close);
  };

  const handleClickPeerClose = (close: boolean) => {
    setPeerOpen(close);
  };

  const handlePeersMoreClose = () => {
    setPeersDrawerOpen(false);
  };

  const handleDelete = (privateChannel: PrivateChannel, event: React.MouseEvent<HTMLButtonElement, MouseEvent>) => {
    event.stopPropagation();
    setDrawerOpen(false);
    setPrivateChannelRow(privateChannel);
    setOpen(true);
  };

  const peerHandleDelete = (peers: PrivateChannelPeers, event: React.MouseEvent<HTMLButtonElement, MouseEvent>) => {
    event.stopPropagation();
    setDrawerOpen(false);
    setPeersRow(peers);
    setPeerOpen(true);
  };
  const handleOnRowClick = (params: any) => {
    handleMore(params.row);
  };

  const handleOnPeersRowClick = (params: any) => {
    handlePeersMore(params.row);
  };

  const handleDeletedItem = (deleted: boolean) => {
    setIsDeleted(deleted);
  };

  const handleDeletedPeerItem = (deleted: boolean) => {
    setIsPeerDeleted(deleted);
  };

  const rows = Array.isArray(data) ? data : [];
  const peersRows = Array.isArray(peersData) ? peersData : [];

  const filteredPrivateChannelsRows = searchId.trim()
    ? rows.filter((row) =>
      row.id?.toString().includes(searchId.trim())
    )
    : rows;


  const filteredPeersRowRows = peerSearchId.trim()
    ? peersRows?.filter((peersRows) =>
      peersRows.id?.toString().includes(peerSearchId.trim())
    )
    : peersRows;

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
      field: "peers",
      headerName: "Number of peers",
      renderCell: (params) => {
        const value = params.row.peers;
        return Array.isArray(value) ? value.length : 0;
      },
    },
    {
      ...dataGridTemplate,
      field: "description",
      headerName: "Description",
      flex: 2,
    },
    {
      ...dataGridTemplate,
      field: "lastUpdated",
      headerName: "Last updated",
      renderCell: (params) => {
        const value = params.row.lastUpdated;
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

  const peerTableHeaders: GridColDef[] = [
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
      field: "owner",
      headerName: "Owner",
      flex: 2,
    },
    {
      ...dataGridTemplate,
      field: "description",
      headerName: "Description",
      flex: 2,
    },
    {
      ...dataGridTemplate,
      field: "lastUpdated",
      headerName: "Last updated",
      renderCell: (params) => {
        const value = params.row.lastUpdated;
        return value && timeConverter(value);
      },
      flex: 2
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
            <IconButton onClick={(event) => peerHandleDelete(params.row, event)}>
              <DeleteIcon />
            </IconButton>
            <IconButton onClick={() => handlePeersMore(params.row)}>
              <MoreVertIcon />
            </IconButton>
          </Box>
        );
      },
    },
  ];

  return (
    <Box flex={1}>
      <Mainheading>Private channels</Mainheading>
      <Subheading>
        These are all of private channels in the network. You can click a private channel or a private channel subscription
        to see details or remove.
      </Subheading>
      <Divider sx={{ marginY: 2 }} />
      <Box display="flex" flexDirection="row" gap={5}>
      <AddButton text="Create private channel" labelUrl="privateChannel"></AddButton>
      <SearchBox searchId={searchId} setSearchId={setSearchId} label="private channel" searchElement="ID"/>
      </Box>
      <Divider style={{ margin: '5px 0', visibility: 'hidden' }} />
      <Subheading>My private channels</Subheading>
      <Divider style={{ margin: '5px 0', visibility: 'hidden' }} />

      <Box
        sx={{
          maxHeight: hasPeersData ? "400px": "hidden",
          overflowY: hasPeersData ? "auto" : "hidden",
        }}
      >
        <DataGrid
          columns={tableHeaders}
          rows={filteredPrivateChannelsRows || []}
          onRowClick={handleOnRowClick}
          loading={isLoading}
          getRowId={(row) => row.id}
          sort={{ field: "lastUpdated", sort: "desc" }}
          slots={{
            noRowsOverlay: CustomEmptyOverlayPrivateChannels,
          }}
        />
        {privateChannelRow && (
          <PrivateChannelsDrawer
            handleMoreClose={handleMoreClose}
            open={drawerOpen}
            privateChannel={privateChannelRow as PrivateChannel}
            handleDeletedItem={handleDeletedItem}
            refetchPrivateChannel={refetch}
          />
        )}
        <DeleteSubDialog
          itemId={privateChannelRow?.id as string}
          handleDialog={handleClickClose}
          open={open}
          actorCommonName={session?.user.commonName as string}
          handleDeletedItem={handleDeletedItem}
          text="Private channel"
          handleMoreClose={handleMoreClose}
        />
      </Box>

      <Divider style={{ margin: "20px 0", visibility: "hidden" }} />
      {hasPeersData && (
        <Box>
          <Box display="flex" flexDirection="row" gap={5}>
          <Subheading>My private channel subscriptions</Subheading>
          <SearchBox searchId={peerSearchId} setSearchId={setPeerSearchId} label="my private channel subscription" searchElement="ID"/>
          </Box>
          <Divider style={{ margin: "10px 0", visibility: "hidden" }} />
          <Box
            sx={{
              maxHeight: hasPeersData ? "400px": "hidden",
              overflowY: hasPeersData ? "auto" : "hidden",
            }}
          >
            <DataGrid
              columns={peerTableHeaders}
              rows={filteredPeersRowRows || []}
              onRowClick={handleOnPeersRowClick}
              loading={isPeersLoading}
              getRowId={(row) => row.id}
              sort={{ field: "lastUpdated", sort: "desc" }}
              slots={{
                noRowsOverlay: CustomEmptyOverlayPrivateChannels
              }}
            />
            {peersRow && (
              <PeersDrawer
                handleMoreClose={handlePeersMoreClose}
                open={peersDrawerOpen}
                peers={peersRow as PrivateChannelPeers}
                handleDeletedItem={handleDeletedPeerItem}
              />
            )}
            <DeleteSubDialog
              itemId={peersRow?.id as string}
              handleDialog={handleClickPeerClose}
              open={peerOpen}
              actorCommonName={session?.user.commonName as string}
              handleDeletedItem={handleDeletedPeerItem}
              text="Subscribed private channel"
              handleMoreClose={handleMoreClose}
            />
          </Box>
        </Box>
      )}
    </Box>
  );
}