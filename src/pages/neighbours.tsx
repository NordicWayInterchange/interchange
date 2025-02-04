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
import {StatusCircle} from "@/components/shared/StatusCircle";
import {CustomEmptyOverlayNeighbours} from "@/components/shared/datagrid/CustomEmptyOverlay";
import {timeConverter} from "@/lib/timeConverter";
import {ExpandedRows} from "@/types/expandedRows";
import {StyledBorderlineSpan, StyledTableHeader} from "@/components/styles/StyledElements";
import ControlConnectionDrawer from "@/components/neighbours/ControlConnectionDrawer";

const Neighbours = () => {
    const {data: session} = useSession();

    const {data: neighbourData, isLoading} = useFetchNeighbours(
        session?.user.commonName as string
    );
    const [firstTableRow, setFirstTableRow] = useState<>(null);
    const [firstTableFieldName, setFirstTableFieldName] = useState<>('');
    const [neighbourRow, setNeighbourRow] = useState<>(null);
    const [expandedRows, setExpandedRows] = useState<ExpandedRows>({});
    const [drawerOpen, setDrawerOpen] = useState<boolean>(false);
    const [highlightedCell, setHighlightedCell] = useState<{
        id: number | null;
        field: string | null;
    }>({id: null, field: null});

    const handleMoreClose = () => {
        setDrawerOpen(false);
    };

    const handleCellClick = (row: any, field: any) => {
        setExpandedRows({});
        const rowId = row.id ? row.id : row.subreq_id;
        setExpandedRows((prev) => ({
            ...prev,
            [rowId]: prev[rowId] === field ? null : field,
        }));
    };

    const handleOnRowClick = (params: GridRowParams) => {
        setNeighbourRow(null);
        setNeighbourRow(params?.row || []);
        setDrawerOpen(true);
    };

    const handleOnFirstTableRowClick = (params: GridRowParams) => {
        setFirstTableRow(null);
        setFirstTableRow(params?.row || []);
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
            headerName: "Name",
            flex: 2
        },
        {
            ...dataGridTemplate,
            field: "capabilities",
            headerName: "Capabilities",
            headerClassName: 'custom-header',
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
                        {Array.isArray(neighbourCapabilities) ?
                            <StyledBorderlineSpan> {neighbourCapabilities.length} </StyledBorderlineSpan> :
                            <StyledBorderlineSpan> {0} </StyledBorderlineSpan>}
                    </Box>
                );
            },
        },
        {
            ...dataGridTemplate,
            field: "ourRequestedSubscriptions",
            headerName: "Our Subscriptions",
            headerClassName: 'custom-header',
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
                        {Array.isArray(ourSubscriptions) ?
                            <StyledBorderlineSpan> {ourSubscriptions.length} </StyledBorderlineSpan> :
                            <StyledBorderlineSpan> {0} </StyledBorderlineSpan>}
                    </Box>
                );
            },
        },
        {
            ...dataGridTemplate,
            field: "neighbourRequestedSubscriptions",
            headerName: "Neighbour Subscriptions",
            headerClassName: 'custom-header',
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
                        {Array.isArray(neighbourSubscriptions) ?
                            <StyledBorderlineSpan> {neighbourSubscriptions.length} </StyledBorderlineSpan> :
                            <StyledBorderlineSpan> {0} </StyledBorderlineSpan>}

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
            flex: 2,
            renderCell: (params) => {
                const value = params.row.lastFailedConnectionAttempt;
                return value && timeConverter(value)
            },
        },
        {
            ...dataGridTemplate,
            field: "lastUpdated",
            headerName: "Last Updated",
            flex: 2,
            renderCell: (params) => {
                const value = params.row.lastUpdated;
                return value && timeConverter(value)
            },
        },
    ];
    return (
        <Box flex={1}>
            <Mainheading>Neighbours</Mainheading>
            <Subheading>
                These are all of neighbours. You can click on each row to see control connection details. You can also click on
                each capabilities, our subscriptions or neighbour subscriptions cell
                to view more information.
            </Subheading>
            <Divider sx={{marginY: 4}}/>
            <Box sx={{height: 450, width: "100%"}}>
                <Box sx={StyledTableHeader}>
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
                            setHighlightedCell({ id: params.id as number, field: params.field });
                            setFirstTableFieldName(params.field);
                        }}
                        getCellClassName={(params) =>
                            (params.field === 'capabilities' || params.field === 'ourRequestedSubscriptions'
                                || params.field === 'neighbourRequestedSubscriptions') &&
                            highlightedCell.id === params.id && highlightedCell.field === params.field
                                ? "highlighted-cell"
                                : ""
                        }
                        onRowClick={handleOnFirstTableRowClick}/>
                    { !(firstTableFieldName === 'capabilities' || firstTableFieldName === 'ourRequestedSubscriptions'
                        || firstTableFieldName === 'neighbourRequestedSubscriptions') &&(<ControlConnectionDrawer
                            handleMoreClose={handleMoreClose}
                            open={drawerOpen}
                            controlConnection={firstTableRow?.controlConnection}/>
                        )}
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
