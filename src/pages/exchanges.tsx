import React, {useState} from 'react';
import {GridColDef} from "@mui/x-data-grid";
import {useSession} from "next-auth/react";
import Mainheading from "@/components/shared/typography/Mainheading";
import {Box, Divider, TextField} from "@mui/material";
import DataGrid from "@/components/shared/datagrid/DataGrid";
import {dataGridTemplate} from "@/components/shared/datagrid/DataGridTemplate";
import Subheading from "@/components/shared/typography/Subheading";
import {
    CustomEmptyOverlayExchanges,
} from "@/components/shared/datagrid/CustomEmptyOverlay";
import {ExpandedRows} from "@/types/expandedRows";
import {StyledBorderlineSpan, StyledTableHeader} from "@/components/styles/StyledElements";
import {useFetchExchanges} from "@/hooks/useFetchExchanges";
import NestedGridExchanges from "@/components/exchanges/NestedGridExchanges";


const Exchanges = () => {
    const {data: session} = useSession();

    const {data: exchangesData, isLoading} = useFetchExchanges(
        session?.user.commonName as string
    );

    const [expandedRows, setExpandedRows] = useState<ExpandedRows>({});

    const [highlightedCell, setHighlightedCell] = useState<{
        id: number | null;
        field: string | null;
    }>({id: null, field: null});
    const [isFlashing, setIsFlashing] = useState(false);
    const [searchId, setSearchId] = useState("");

    const handleCellClick = (row: any, field: any) => {
        setExpandedRows({});
        const rowId = row.id;
        setExpandedRows((prev) => ({
            ...prev,
            [rowId]: prev[rowId] === field ? null : field,
        }));
        setIsFlashing(true);
        setTimeout(() => setIsFlashing(false), 300);
    };


    const tableHeaders: GridColDef[] = [
        {
            ...dataGridTemplate,
            field: "id",
            headerName: "ID",
            flex: 2
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
                            const rowId = params.row.id;
                            setExpandedRows({});
                            handleCellClick("bindings", rowId)
                        }}
                    >
                        {Array.isArray(bindings) ?
                            <StyledBorderlineSpan> {bindings.length} </StyledBorderlineSpan> :
                            <StyledBorderlineSpan> : 0 </StyledBorderlineSpan>}
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

    const rows = Array.isArray(exchangesData) ? exchangesData : [];

    const filteredRows = searchId.trim()
        ? rows.filter((row) =>
            row.id?.toString().includes(searchId.trim())
        )
        : rows;


    return (
        <Box flex={1}>
            <Mainheading>Exchanges</Mainheading>
            <Subheading>
                These are all of qpid exchanges. You can click on each row to see more information.
            </Subheading>
            <Divider sx={{marginY: 4}}/>
            <Box sx={{height: 450, width: "100%"}}>
                <Box sx={StyledTableHeader}>
                    <TextField
                        label="Search by ID"
                        variant="outlined"
                        value={searchId}
                        onChange={(e) => setSearchId(e.target.value)}
                        style={{ marginBottom: 16, marginTop: -25 }}
                        type="text"
                    />
                    <DataGrid
                        columns={tableHeaders}
                        rows={filteredRows}
                        loading={isLoading}
                        getRowId={(row) => row.id}
                        sort={{field: "id", sort: "desc"}}
                        slots={{
                            noRowsOverlay: CustomEmptyOverlayExchanges
                        }}
                        onCellClick={(params) => {
                            setHighlightedCell({id: params.id as number, field: params.field});
                        }}
                        getCellClassName={(params) =>
                            (params.field === 'bindings') &&
                            highlightedCell.id === params.id && highlightedCell.field === params.field
                                ? "highlighted-cell"
                                : ""
                        }/>

                </Box>
            </Box>
            {Object.keys(expandedRows).map((rowId) => {
                const row = Array.isArray(exchangesData) ? exchangesData.find((item) => item.id === expandedRows[rowId]) : null;
                const field = expandedRows[rowId];

                return (
                    <Box key={rowId}>
                        <NestedGridExchanges
                            nestedBindingData={row}
                            field={field}
                            isFlashing={isFlashing}
                        />
                    </Box>
                );
            })}
        </Box>
    );
};
export default Exchanges;
