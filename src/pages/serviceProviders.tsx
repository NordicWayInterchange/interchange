import Mainheading from "@/components/shared/typography/Mainheading";
import {Box, Divider} from "@mui/material";
import Subheading from "@/components/shared/typography/Subheading";
import React, {useState} from "react";
import {GridColDef} from "@mui/x-data-grid";
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

export default function ServiceProviders() {
    const {data: session} = useSession();

    const {data: serviceProviderData, isLoading} = useFetchServiceProviders(
        session?.user.commonName as string
    );

    const [serviceProviderRow, setServiceProviderRow] = useState<ServiceProviderSubscriptions | ServiceProviderDeliveries | ServiceProviderCapabilities | null>(null);
    const [highlightedCell, setHighlightedCell] = useState<{
        id: number | null;
        field: string | null;
    }>({id: null, field: null});


    const serviceProviderTableHeaders: GridColDef[] = [
        {
            ...dataGridTemplate,
            field: "name",
            headerName: "Domain name",
        },
        {
            ...dataGridTemplate,
            field: "subscriptions",
            headerName: "Subscriptions",
            renderCell: (params) => {
                const serviceSubscriptions = params.row.subscriptions;
                return (
                    <Box
                        style={{cursor: "pointer"}}
                        onClick={() => {
                            setServiceProviderRow(null);
                        }}
                    >
                        {Array.isArray(serviceSubscriptions) ? serviceSubscriptions.length : 0}
                    </Box>
                );
            },
        },
        {
            ...dataGridTemplate,
            field: "capabilities",
            headerName: "Capabilities",
            renderCell: (params) => {
                const serviceProviderCapabilities = params.row.capabilities;
                return (
                    <Box
                        style={{cursor: "pointer"}}
                        onClick={() => {
                            setServiceProviderRow(null);
                        }}
                    >
                        {Array.isArray(serviceProviderCapabilities) ? serviceProviderCapabilities.length : 0}
                    </Box>
                );
            },
        },
        {
            ...dataGridTemplate,
            field: "deliveries",
            headerName: "Deliveries",
            renderCell: (params) => {
                const serviceProviderDeliveries = params.row.deliveries;
                return (
                    <Box
                        style={{cursor: "pointer"}}
                        onClick={() => {
                            setServiceProviderRow(null);
                        }}
                    >
                        {Array.isArray(serviceProviderDeliveries) ? serviceProviderDeliveries.length : 0}
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
                    These are all of all service providers. You can click on capabilities, subscriptions or deliveries cell
                    to view more information.
                </Subheading>
                <Divider sx={{marginY: 3}}/>
                <Box sx={{height: 400, width: "100%"}}>
                    <Box>
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
                                highlightedCell.id === params.id && highlightedCell.field === params.field
                                    ? "highlighted-cell"
                                    : ""
                            }/>
                    </Box>
                </Box>
            </Box>
        </>
    );
}
