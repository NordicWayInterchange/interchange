import React, {useState} from 'react';
import {GridColDef, GridRowParams} from "@mui/x-data-grid";
import {useSession} from "next-auth/react";
import Mainheading from "@/components/shared/typography/Mainheading";
import {Box, Divider} from "@mui/material";
import DataGrid from "@/components/shared/datagrid/DataGrid";
import {dataGridTemplate} from "@/components/shared/datagrid/DataGridTemplate";
import Subheading from "@/components/shared/typography/Subheading";
import {
    CustomEmptyOverlayExchanges,
} from "@/components/shared/datagrid/CustomEmptyOverlay";
import {ExpandedRows} from "@/types/expandedRows";
import {StyledBorderlineSpan, StyledTableHeader} from "@/components/styles/StyledElements";
import ControlConnectionDrawer from "@/components/neighbours/ControlConnectionDrawer";
import {IFirstNeighbourTable} from "@/interfaces/IFirstNeighbourTable";
import {ControlConnection} from "@/types/neighbours";
import {useFetchExchanges} from "@/hooks/useFetchExchanges";

const Exchanges = () => {
    const {data: session} = useSession();

    const {data: exchangesData, isLoading} = useFetchExchanges(
        session?.user.commonName as string
    );
    const [firstTableRow, setFirstTableRow] = useState<IFirstNeighbourTable | null>(null);
    const [firstTableFieldName, setFirstTableFieldName] = useState('');
    const [secondTableRow, setSecondTableRow] = useState(null);
    const [expandedRows, setExpandedRows] = useState<ExpandedRows>({});
    const [firstDrawerOpen, setFirstDrawerOpen] = useState<boolean>(false);
    const [secondDrawerOpen, setSecondDrawerOpen] = useState<boolean>(false);
    const [highlightedCell, setHighlightedCell] = useState<{
        id: number | null;
        field: string | null;
    }>({id: null, field: null});
    const [isFlashing, setIsFlashing] = useState(false);

    const handleFirstDrawerClose = () => {
        setFirstDrawerOpen(false);
    };

    const handleSecondTableClose = () => {
        setSecondDrawerOpen(false);
    };

    const handleCellClick = (row: any, field: any) => {
        setExpandedRows({});
        const rowId = row.id ? row.id : row.subreq_id;
        setExpandedRows((prev) => ({
            ...prev,
            [rowId]: prev[rowId] === field ? null : field,
        }));
        setIsFlashing(true);
        setTimeout(() => setIsFlashing(false), 300);
    };

    const handleOnSecondTableRowClick = (params: GridRowParams) => {
        setSecondTableRow(null);
        setSecondTableRow(params?.row || []);
        setSecondDrawerOpen(true);
    };

    const handleOnFirstTableRowClick = (params: GridRowParams) => {
        setFirstTableRow(null);
        setFirstTableRow(params?.row || []);
        setFirstDrawerOpen(true);
    };

    const tableHeaders: GridColDef[] = [
        {
            ...dataGridTemplate,
            field: "id",
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
            field: "bindings",
            headerName: "Bindings",
            headerClassName: 'custom-header',
            renderCell: (params) => {
                const bindings = params.row.bindings;
                return (
                    <Box
                        style={{cursor: "pointer"}}
                        onClick={() => {
                            setSecondTableRow(null);
                            handleCellClick(params.row.capabilities, "capabilities")
                        }}
                    >
                        {Array.isArray(bindings) ?
                            <StyledBorderlineSpan> {bindings.length} </StyledBorderlineSpan> :
                            <StyledBorderlineSpan> {0} </StyledBorderlineSpan>}
                    </Box>
                );
            },
        },
        {
            ...dataGridTemplate,
            field: "type",
            headerName: "Type",
            flex: 1
        },
        {
            ...dataGridTemplate,
            field: "durable",
            headerName: "Durable",
            flex: 1
        }
    ];
    const displayControlConnectionDrawer = firstTableRow && firstTableRow?.controlConnection && !(firstTableFieldName === 'capabilities' || firstTableFieldName === 'ourRequestedSubscriptions'
        || firstTableFieldName === 'neighbourRequestedSubscriptions');
    return (
        <Box flex={1}>
            <Mainheading>Exchanges</Mainheading>
            <Subheading>
                These are all of qpid exchanges. You can click on each row to see more information.
            </Subheading>
            <Divider sx={{marginY: 4}}/>
            <Box sx={{height: 450, width: "100%"}}>
                <Box sx={StyledTableHeader}>
                    <DataGrid
                        columns={tableHeaders}
                        rows={exchangesData || []}
                        loading={isLoading}
                        getRowId={(row) => row.neighbour_id}
                        sort={{field: "lastUpdated", sort: "desc"}}
                        slots={{
                            noRowsOverlay: CustomEmptyOverlayExchanges
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
                    {displayControlConnectionDrawer && (<ControlConnectionDrawer
                            handleMoreClose={handleFirstDrawerClose}
                            open={firstDrawerOpen}
                            controlConnection={firstTableRow?.controlConnection ?? ({} as ControlConnection)}/>

                    )}
                </Box>
            </Box>

        </Box>
    );
};
export default Exchanges;
