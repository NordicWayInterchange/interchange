import Mainheading from "@/components/shared/typography/Mainheading";
import {Box, Divider} from "@mui/material";
import Subheading from "@/components/shared/typography/Subheading";
import React, {useState} from "react";
import {GridColDef, GridRowParams} from "@mui/x-data-grid";
import {dataGridTemplate} from "@/components/shared/datagrid/DataGridTemplate";
import DataGrid from "@/components/shared/datagrid/DataGrid";
import {
    CustomEmptyOverlayServiceProviders
} from "@/components/shared/datagrid/CustomEmptyOverlay";
import {useSession} from "next-auth/react";
import {useFetchServiceProviders} from "@/hooks/useFetchServiceProviders";
import {
    ServiceProviderCapabilities,
    ServiceProviderDeliveries,
    ServiceProviderSubscriptions
} from "@/types/serviceProviders";
import {ExpandedRows} from "@/types/expandedRows";
import {StyledBorderlineSpan, StyledTableHeader} from "@/components/styles/StyledElements";
import NestedGridServiceProviders from "@/components/serviceProviders/NestedGridServiceProviders";

export default function ServiceProviders() {
    const {data: session} = useSession();
    const [expandedRows, setExpandedRows] = useState<ExpandedRows>({});
    const [drawerOpen, setDrawerOpen] = useState<boolean>(false);

    const {data: serviceProviderData, isLoading} = useFetchServiceProviders(
        session?.user.commonName as string
    );

    const [serviceProviderRow, setServiceProviderRow] = useState<ServiceProviderSubscriptions | ServiceProviderDeliveries | ServiceProviderCapabilities | null>(null);
    const [highlightedCell, setHighlightedCell] = useState<{
        id: number | null;
        field: string | null;
    }>({id: null, field: null});

    const [isFlashing, setIsFlashing] = useState(false);

    const handleMoreClose = () => {
        setDrawerOpen(false);
    };

    const handleCellClick = (row: any, field: any, rowId: number) => {
        setExpandedRows({});
        setExpandedRows((prev) => ({
            ...prev,
            [rowId]: prev[rowId] === field ? null : field,
        }));
        setIsFlashing(true);
        setTimeout(() => setIsFlashing(false), 300);
    };

    const handleOnRowClick = (params: GridRowParams) => {
        setServiceProviderRow(null);
        setServiceProviderRow(params?.row || []);
        setDrawerOpen(true);
    };

    const serviceProviderTableHeaders: GridColDef[] = [
        {
            ...dataGridTemplate,
            field: "id",
            headerName: "ID",
        },
        {
            ...dataGridTemplate,
            field: "name",
            flex: 4,
            headerName: "Domain name"
        },
        {
            ...dataGridTemplate,
            field: "subscriptions",
            headerName: "Subscriptions",
            headerClassName: 'custom-header',
            renderCell: (params) => {
                const serviceSubscriptions = params.row.subscriptions;
                return (
                    <Box
                        style={{cursor: "pointer"}}
                        onClick={() => {
                            const rowId = params.row.id;
                            setServiceProviderRow(null);
                            handleCellClick(params.row.subscriptions, "subscriptions", rowId)
                        }}
                    >
                        {Array.isArray(serviceSubscriptions) ?
                            <StyledBorderlineSpan> {serviceSubscriptions.length} </StyledBorderlineSpan> :
                            <StyledBorderlineSpan> : 0 </StyledBorderlineSpan>}
                    </Box>
                );
            },
        },
        {
            ...dataGridTemplate,
            field: "capabilities",
            headerName: "Capabilities",
            headerClassName: 'custom-header',
            renderCell: (params) => {
                const serviceProviderCapabilities = params.row.capabilities;
                return (
                    <Box
                        style={{cursor: "pointer"}}
                        onClick={() => {
                            const rowId = params.row.id;
                            setServiceProviderRow(null);
                            handleCellClick(params.row.capabilities, "capabilities", rowId)
                        }}
                    >
                        {Array.isArray(serviceProviderCapabilities) ?
                            <StyledBorderlineSpan> {serviceProviderCapabilities.length} </StyledBorderlineSpan> :
                            <StyledBorderlineSpan> {0} </StyledBorderlineSpan> }
                    </Box>
                );
            },
        },
        {
            ...dataGridTemplate,
            field: "deliveries",
            headerName: "Deliveries",
            headerClassName: 'custom-header',
            renderCell: (params) => {
                const serviceProviderDeliveries = params.row.deliveries;
                return (
                    <Box
                        style={{cursor: "pointer"}}
                        onClick={() => {
                            const rowId = params.row.id;
                            setServiceProviderRow(null);
                            handleCellClick(params.row.deliveries, "deliveries", rowId)
                        }}
                    >
                        {Array.isArray(serviceProviderDeliveries) ?
                            <StyledBorderlineSpan> {serviceProviderDeliveries.length}  </StyledBorderlineSpan> :
                            <StyledBorderlineSpan> {0} </StyledBorderlineSpan> }
                    </Box>
                );
            },
        }
    ];

    return (
        <>
            <Box flex={1}>
                <Mainheading>Service providers</Mainheading>
                <Subheading>
                    These are all of all service providers. You can click on subscriptions, capabilities or deliveries cell
                    to view more information.
                </Subheading>
                <Divider sx={{marginY: 3}}/>
                <Box sx={{height: 450, width: "100%"}}>
                    <Box sx={StyledTableHeader}>
                        <DataGrid
                            columns={serviceProviderTableHeaders}
                            rows={serviceProviderData || []}
                            loading={isLoading}
                            getRowId={(row) => row.id}
                            sort={{field: "lastUpdated", sort: "desc"}}
                            slots={{
                                noRowsOverlay: CustomEmptyOverlayServiceProviders
                            }}
                            onCellClick={(params) => {
                                setHighlightedCell({ id: params.id as number, field: params.field });

                            }}
                            getCellClassName={(params) =>
                                (params.field === 'subscriptions' || params.field === 'capabilities'
                                    || params.field === 'deliveries') &&
                                highlightedCell.id === params.id && highlightedCell.field === params.field
                                    ? "highlighted-cell"
                                    : ""
                            }/>
                    </Box>
                </Box>
                {Object.keys(expandedRows).map((rowId) => {
                    const row = Array.isArray(serviceProviderData) ? serviceProviderData.find((item) => item.id === parseInt(rowId)) : null;
                    const field = expandedRows[rowId];

                    if (!row) {
                        return null;
                    }
                    return (
                        <Box key={rowId}>
                            <NestedGridServiceProviders
                                row={row}
                                field={field}
                                drawerOpen={drawerOpen}
                                serviceProviderRow={serviceProviderRow}
                                handleMoreClose={handleMoreClose}
                                handleOnRowClick={handleOnRowClick}
                                isFlashing={isFlashing}
                            />
                        </Box>
                    );
                })}
            </Box>
        </>
    );
}
