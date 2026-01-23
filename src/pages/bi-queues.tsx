import { Box } from "@mui/system";
import React, { useState } from "react";
import { dataGridTemplate } from "@/components/shared/datagrid/DataGridTemplate";
import { GridColDef } from "@mui/x-data-grid";
import BiQueueEndpointDrawer from "@/components/shared/drawer/BiqueueEndpointDrawer";
import {Chip, Divider, IconButton} from "@mui/material";
import Mainheading from "@/components/shared/typography/Mainheading";
import DataGrid from "@/components/shared/datagrid/DataGrid";
import Subheading from "@/components/shared/typography/Subheading";
import {BiQueueEndpointsApi} from "@/types/BiQueueResponse";
import {useFetchBiQueueEndpoints} from "@/hooks/UseFetchBiQueueEndpoint";
import {CustomFooter} from "@/components/shared/datagrid/CustomFooter";
import {messageTypeChips} from "@/lib/statusChips";
import {CustomEmptyOverlayBiqueueEndpoints} from "@/components/shared/datagrid/CustomEmptyOverlay";
import {useSession} from "next-auth/react";
import MoreVertIcon from "@mui/icons-material/MoreVert";

export default function BiQueues() {
    const {data: session} = useSession();
    const { data, isLoading } = useFetchBiQueueEndpoints(session?.user.commonName as string);
    const [biqueueEndpointRow, setBiqueueEndpointRow] = useState<BiQueueEndpointsApi>();

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