import Mainheading from "@/components/shared/typography/Mainheading";
import {Box, Divider} from "@mui/material";
import Subheading from "@/components/shared/typography/Subheading";
import React, {useEffect, useState} from "react";
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
    ServiceProviderDeliveries, ServiceProviderPrivatechannels,
    ServiceProviderSubscriptions
} from "@/types/serviceProviders";
import {ExpandedRows} from "@/types/expandedRows";
import {StyledBorderlineSpan, StyledTableHeader} from "@/components/styles/StyledElements";
import NestedGridServiceProviders from "@/components/serviceProviders/NestedGridServiceProviders";
import BiQueueEndpointDrawer from "@/components/shared/drawer/BiqueueEndpointDrawer";


export default function ServiceProviders() {
    const {data: session} = useSession();
    const [expandedRows, setExpandedRows] = useState<ExpandedRows>({});
    const [drawerOpen, setDrawerOpen] = useState<boolean>(false);
    const [open, setOpen] = React.useState(false);
    const {data: serviceProviderData, isLoading, refetch} = useFetchServiceProviders(
        session?.user.commonName as string
    );

    const [serviceProviderRow, setServiceProviderRow] = useState<any>(null);
    const [highlightedCell, setHighlightedCell] = useState<{
        id: number | null;
        field: string | null;
    }>({id: null, field: null});

    const [isFlashing, setIsFlashing] = useState(false);

    const handleMoreClose = () => {
        setDrawerOpen(false);
    };

    useEffect(() => {
        if (open) {
            refetch();
        }
    }, [open, refetch]);

    const handleCellClick = (field: any, rowId: number) => {
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

    const handleOnRowWithBiQueueClick = (params: GridRowParams) => {
        console.log(params);
        setServiceProviderRow(null);
        setServiceProviderRow(params?.row || []);
        setOpen(true);
    };

    const handleOpen = (value: boolean) => () => setOpen(value);

    const handleClose = () => setOpen(false);

    const serviceProviderTableHeaders: GridColDef[] = [
        {
            ...dataGridTemplate,
            field: "id",
            headerName: "ID",
            renderCell: (params) => {
                const serviceIds = params.row.id;
                return (
                    <Box
                        onClick={() => {
                            setServiceProviderRow(null);
                            setServiceProviderRow(params?.row || []);
                            setOpen(true);
                        }}
                    >{serviceIds}
                    </Box>
                );
            },
        },
        {
            ...dataGridTemplate,
            field: "name",
            flex: 4,
            headerName: "Domain name",
            renderCell: (params) => {
                const serviceNames = params.row.name;
                return (
                    <Box
                        onClick={() => {
                            setServiceProviderRow(null);
                            setServiceProviderRow(params?.row || []);
                            setOpen(true);
                        }}
                    >{serviceNames}
                    </Box>
                );
            },
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
                            handleCellClick("subscriptions", rowId)
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
                            handleCellClick("capabilities", rowId)
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
                            handleCellClick("deliveries", rowId)
                        }}
                    >
                        {Array.isArray(serviceProviderDeliveries) ?
                            <StyledBorderlineSpan> {serviceProviderDeliveries.length}  </StyledBorderlineSpan> :
                            <StyledBorderlineSpan> {0} </StyledBorderlineSpan> }
                    </Box>
                );
            },
        },
        {
            ...dataGridTemplate,
            field: "privateChannels",
            headerName: "Private channels",
            headerClassName: 'custom-header',
            renderCell: (params) => {
                const serviceProviderPrivatechannels = params.row.privatechannels;
                return (
                    <Box
                        style={{cursor: "pointer"}}
                        onClick={() => {
                            const rowId = params.row.id;
                            setServiceProviderRow(null);
                            handleCellClick("privateChannels", rowId)
                        }}
                    >
                        {Array.isArray(serviceProviderPrivatechannels) ?
                            <StyledBorderlineSpan> {serviceProviderPrivatechannels.length}  </StyledBorderlineSpan> :
                            <StyledBorderlineSpan> {0} </StyledBorderlineSpan> }
                    </Box>
                );
            },
        },
        {
            ...dataGridTemplate,
            field: "privateChannelsPeer",
            headerName: "Private channel peers",
            headerClassName: 'custom-header',
            renderCell: (params) => {
                const serviceProviderPrivatechannelsPeer = params.row.privatechannelsPeer;
                return (
                    <Box
                        style={{cursor: "pointer"}}
                        onClick={() => {
                            const rowId = params.row.id;
                            setServiceProviderRow(null);
                            handleCellClick("privateChannelsPeer", rowId)
                        }}
                    >
                        {Array.isArray(serviceProviderPrivatechannelsPeer) ?
                            <StyledBorderlineSpan> {serviceProviderPrivatechannelsPeer.length}  </StyledBorderlineSpan> :
                            <StyledBorderlineSpan> {0} </StyledBorderlineSpan> }
                    </Box>
                );
            },
        },
        {
            ...dataGridTemplate,
            field: "biconsumer",
            headerName: "Bi-consumer",
            renderCell: (params) => {
                const serviceBiconsumer = params.row.biconsumer;
                console.log(serviceBiconsumer);
                return (
                    <Box
                        onClick={() => {
                            setServiceProviderRow(null);
                            setServiceProviderRow(params?.row || []);
                            setOpen(true);
                        }}
                    > {serviceBiconsumer?.toString()}
                    </Box>
                );
            },
        },
    ];

    const displayBiQueueEndpointDrawer = serviceProviderRow && serviceProviderRow?.biqueueEndpoint;
    return (
        <>
            <Box flex={1}>
                <Mainheading>Service providers</Mainheading>
                <Subheading>
                    These are all of all service providers. You can click on each underlined data cell to see the details.
                    You can also click on each service provider&#39;s row to see bi-queue endpoints, if available.
                </Subheading>
                <Divider sx={{marginY: 3}}/>
                <Box sx={{height: 450, width: "100%"}}>
                    <Box sx={StyledTableHeader}>
                        <DataGrid
                            columns={serviceProviderTableHeaders}
                            rows={serviceProviderData || []}
                            loading={isLoading}
                            getRowId={(row) => row.id}
                            sort={{field: "id", sort: "desc"}}
                            slots={{
                                noRowsOverlay: CustomEmptyOverlayServiceProviders
                            }}
                            onCellClick={(params) => {
                                setHighlightedCell({ id: params.id as number, field: params.field });
                            }}
                            getCellClassName={(params) =>
                                (params.field === 'subscriptions' || params.field === 'capabilities'
                                    || params.field === 'deliveries' || params.field === 'privateChannels' || params.field === 'privateChannelsPeer' ) &&
                                highlightedCell.id === params.id && highlightedCell.field === params.field
                                    ? "highlighted-cell"
                                    : ""
                            }/>
                    </Box>
                    {displayBiQueueEndpointDrawer && ( <BiQueueEndpointDrawer
                            biQueueEndpoint={displayBiQueueEndpointDrawer}
                            open={open}
                            onClose={handleClose}
                        />
                    )}
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

