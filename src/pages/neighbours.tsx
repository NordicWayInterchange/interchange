import React, {useState} from 'react';
import {GridColDef, GridRowParams} from "@mui/x-data-grid";
import {useFetchNeighbours} from "@/hooks/useFetchNeighbours";
import {useSession} from "next-auth/react";
import Mainheading from "@/components/shared/typography/Mainheading";
import {Box, ChipProps, Divider} from "@mui/material";
import {Chip} from "@/components/shared/Chip";
import {connectionStatusChips} from "@/lib/statusChips";
import DataGrid from "@/components/shared/datagrid/DataGrid";
import {dataGridTemplate} from "@/components/shared/datagrid/DataGridTemplate";
import {Neighbours} from "@/types/neighbours";
import NestedGridNeighbours from "@/components/neighbours/NestedGridNeighbours";
import Subheading from "@/components/shared/typography/Subheading";


const Neighbours = () => {
    const {data: session} = useSession();

    const {data: neighbourData, isLoading} = useFetchNeighbours(
        session?.user.commonName as string
    );
    const [neighbourRow, setNeighbourRow] = useState<Neighbours>();
    const [expandedRows, setExpandedRows] = useState({});
    const [drawerOpen, setDrawerOpen] = useState<boolean>(false);

    const handleMoreClose = () => {
        setDrawerOpen(false);
    };

    const handleCellClick = (row, field) => {
        const rowId = row.id ? row.id : row.subreq_id;
        setExpandedRows((prev) => ({
            ...prev,
            [rowId]: prev[rowId] === field ? null : field,
        }));
    };

    const handleOnRowClick = (params: GridRowParams) => {
        handleMore(params?.row || []);
    };

    const handleMore = (neighbour) => {
        console.log('neighbour', neighbour)
        setNeighbourRow(neighbour);
        setDrawerOpen(true);
    };

    const tableHeaders: GridColDef[] = [
        {
            ...dataGridTemplate,
            field: "neighbour_id",
            headerName: "ID"
        },
        {
            ...dataGridTemplate,
            field: "name",
            headerName: "Name"
        },
        {
            ...dataGridTemplate,
            field: "capabilities",
            headerName: "Capabilities",
            renderCell: (params) => {
                const neighbourCapabilities = params.row.capabilities.capabilities;
                return (
                    <Box
                        style={{cursor: "pointer"}}
                        onClick={() => handleCellClick(params.row.capabilities, "capabilities")}
                    >
                        {Array.isArray(neighbourCapabilities) ? neighbourCapabilities.length : 0}
                    </Box>
                );
            },
        },
        {
            ...dataGridTemplate,
            field: "ourRequestedSubscriptions",
            headerName: "Our Subscriptions",
            renderCell: (params) => {
                const ourSubscriptions = params.row.ourRequestedSubscriptions.subscriptions;
                return (
                    <Box
                        style={{cursor: "pointer"}}
                        onClick={() => handleCellClick(params.row.ourRequestedSubscriptions, "ourRequestedSubscriptions")}
                    >
                        {Array.isArray(ourSubscriptions) ? ourSubscriptions.length : 0}
                    </Box>
                );
            },
        },
        {
            ...dataGridTemplate,
            field: "neighbourRequestedSubscriptions",
            headerName: "Neighbour Subscriptions",
            renderCell: (params) => {
                const neighbourSubscriptions = params.row.neighbourRequestedSubscriptions.subscriptions;
                return (
                    <Box
                        style={{cursor: "pointer"}}
                        onClick={() => handleCellClick(params.row.neighbourRequestedSubscriptions, "neighbourRequestedSubscriptions")}
                    >
                        {Array.isArray(neighbourSubscriptions) ? neighbourSubscriptions.length : 0}

                    </Box>
                );
            },
        },
        {
            ...dataGridTemplate,
            field: "connectionStatus",
            headerName: "Connection Status",
            renderCell: (cell) => {
                return (
                    <Chip
                        color={
                            connectionStatusChips[
                                cell.value as keyof typeof connectionStatusChips
                                ] as ChipProps['color']
                        }
                        label={cell.value}
                    />
                );
            },
        },
    ];

    return (
        <Box flex={1}>
            <Mainheading>Neighbours</Mainheading>
            <Subheading>
                These are all of neighbours. You can click a row to view more information.
            </Subheading>
            <Divider sx={{marginY: 4}}/>
            <Box sx={{height: 400, width: "100%"}}>
                <DataGrid
                    columns={tableHeaders}
                    rows={neighbourData || []}
                    loading={isLoading}
                    getRowId={(row) => row.neighbour_id}
                    sort={{field: "lastUpdated", sort: "desc"}}
                />
            </Box>
            {Object.keys(expandedRows).map((rowId) => {
                const row = Array.isArray(neighbourData) ? neighbourData.find((item) => item.neighbour_id === parseInt(rowId)) : null;
                const field = expandedRows[rowId];

                if (!row) {
                    return null;
                }

                return (
                    <Box key={rowId}>
                        <NestedGridNeighbours
                            row={row}
                            field={field}
                            drawerOpen={drawerOpen}
                            neighbourRow={neighbourRow}
                            handleMoreClose={handleMoreClose}
                            handleOnRowClick={handleOnRowClick}
                        />
                    </Box>
                );
            })}
        </Box>
    );
};

export default Neighbours;
