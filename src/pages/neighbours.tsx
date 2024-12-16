import React, {useState} from 'react';
import {GridColDef, GridRowParams} from "@mui/x-data-grid";
import {useFetchNeighbours} from "@/hooks/useFetchNeighbours";
import {useSession} from "next-auth/react";
import Mainheading from "@/components/shared/typography/Mainheading";
import {Box, Divider} from "@mui/material";
import DataGrid from "@/components/shared/datagrid/DataGrid";
import {dataGridTemplate} from "@/components/shared/datagrid/DataGridTemplate";
import NestedGridNeighbours from "@/components/neighbours/NestedGridNeighbours";
import Subheading from "@/components/shared/typography/Subheading";
import {Neighbours} from "@/types/neighbours";
import {StatusCircle} from "@/components/shared/StatusCircle";
import {CustomEmptyOverlayNeighbours} from "@/components/shared/datagrid/CustomEmptyOverlay";
import {timeConverter} from "@/lib/timeConverter";

const Neighbours = () => {
    const {data: session} = useSession();

    const {data: neighbourData, isLoading} = useFetchNeighbours(
        session?.user.commonName as string
    );
    const [neighbourRow, setNeighbourRow] = useState<Neighbours>(null);
    const [expandedRows, setExpandedRows] = useState({});
    const [drawerOpen, setDrawerOpen] = useState<boolean>(false);
    const [selectedColumn, setSelectedColumn] = useState<string | null>(null);

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
        setNeighbourRow(null);
        setNeighbourRow(neighbour);
        setDrawerOpen(true);
    };

    const tableHeaders: GridColDef[] = [
        {
            ...dataGridTemplate,
            field: "neighbour_id",
            headerName: "ID",
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
            headerClassName: 'custom-header',
            cellClassName: (params) => (params.field === selectedColumn ? 'selected-column' : ''),
            renderCell: (params) => {
                const neighbourCapabilities = params.row.capabilities.capabilities;
                return (
                    <Box
                        style={{cursor: "pointer"}}
                        onClick={() => {
                            setNeighbourRow(null);
                            handleCellClick(params.row.capabilities, "capabilities")
                        }}
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
            headerClassName: 'custom-header',
            cellClassName: (params) => (params.field === selectedColumn ? 'selected-column' : ''),
            renderCell: (params) => {
                const ourSubscriptions = params.row.ourRequestedSubscriptions.subscriptions;
                return (
                    <Box
                        style={{cursor: "pointer"}}
                        onClick={() => {
                            setNeighbourRow(null);
                            handleCellClick(params.row.ourRequestedSubscriptions, "ourRequestedSubscriptions")
                        }}
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
            headerClassName: 'custom-header',
            cellClassName: (params) => (params.field === selectedColumn ? 'selected-column' : ''),
            renderCell: (params) => {
                const neighbourSubscriptions = params.row.neighbourRequestedSubscriptions.subscriptions;
                return (
                    <Box
                        style={{cursor: "pointer"}}
                        onClick={() => {
                            setNeighbourRow(null);
                            handleCellClick(params.row.neighbourRequestedSubscriptions, "neighbourRequestedSubscriptions")
                        }}
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
            renderCell: (params) => {
                return (
                    <Box style={{marginBottom: '10px'}}>
                        <StatusCircle status={params.value}/>
                        <span style={{marginLeft: '8px'}}>{params.value}</span>
                    </Box>
                );
            },
        },
        {
            ...dataGridTemplate,
            field: "lastFailedConnectionAttempt",
            headerName: "Last failed connection attempt",
            renderCell: (params) => {
                const value = params.row.lastFailedConnectionAttempt;
                return new Date(value).toLocaleString()
            },
        },
        {
            ...dataGridTemplate,
            field: "lastUpdated",
            headerName: "Last Updated",
            renderCell: (params) => {
                const value = params.row.lastUpdated;
                return new Date(value).toLocaleString()
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
                <Box
                    sx={{
                        height: 400,
                        width: '100%',
                        '& .selected-column': {
                            backgroundColor: '#F8DEDE',
                            color: 'red',
                        },
                        '& .custom-header': {
                            backgroundColor: 'headerBackgroundColor',
                            color: '#fff',
                            fontWeight: 'bold',
                        },
                    }}
                >
                    <DataGrid
                        columns={tableHeaders}
                        rows={neighbourData || []}
                        loading={isLoading}
                        getRowId={(row) => row.neighbour_id}
                        sort={{field: "lastUpdated", sort: "desc"}}
                        slots={{
                            noRowsOverlay: CustomEmptyOverlayNeighbours
                        }}
                        onCellClick={(params) => {
                            setSelectedColumn(params.field);
                        }}/>
                </Box>
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
