import Mainheading from "@/components/shared/typography/Mainheading";
<<<<<<< HEAD
import {Box} from "@mui/material";

export default function ServiceProviders() {
    return (
        <>
            <Box flex={1}>
                <Mainheading>Service Providers</Mainheading>
=======
import {Box, Divider} from "@mui/material";
import Subheading from "@/components/shared/typography/Subheading";
import React, {useState} from "react";
import TabMenu from "@/components/shared/TabMenu";
import {GridColDef} from "@mui/x-data-grid";
import {dataGridTemplate} from "@/components/shared/datagrid/DataGridTemplate";
import {Capability, Subscription} from "@/types/neighbours";
import DataGrid from "@/components/shared/datagrid/DataGrid";
import {CustomEmptyOverlayNeighbours} from "@/components/shared/datagrid/CustomEmptyOverlay";
import {useFetchNeighbours} from "@/hooks/useFetchNeighbours";
import {useSession} from "next-auth/react";

export default function ServiceProviders() {
    const {data: session} = useSession();

   /* const {data: serviceProviderData, isLoading} = useFetchServiceProvider(
        session?.user.commonName as string
    );*/

    const [serviceProviderRow, setServiceProviderRow] = useState<Subscription | Capability | null>(null);

    const serviceProviderTableHeader: GridColDef[] = [
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
                    These are all of all service providers. You can click a row to view more information.
                </Subheading>
                <Divider sx={{marginY: 3}}/>
                <Box sx={{height: 400, width: "100%"}}>
                    <Box>
                        <DataGrid
                            columns={serviceProviderTableHeader}
                            rows={neighbourData || []}
                            loading={isLoading}
                            getRowId={(row) => row.neighbour_id}
                            sort={{field: "lastUpdated", sort: "desc"}}
                            slots={{
                                noRowsOverlay: CustomEmptyOverlayNeighbours
                            }}
                        />
                    </Box>
                </Box>
>>>>>>> 10f79bb (Added serviceProvider main page)
            </Box>
        </>
    );
}
