import {GridColDef} from "@mui/x-data-grid";
import {timeConverter} from "@/lib/timeConverter";
import {dataGridTemplate} from "@/components/shared/datagrid/DataGridTemplate";
import {Chip} from "@/components/shared/Chip";
import {messageTypeChips, statusChips} from "@/lib/statusChips";
import {Box, ChipProps, Divider} from "@mui/material";
import Mainheading from "@/components/shared/typography/Mainheading";
import Subheading from "@/components/shared/typography/Subheading";
import DataGrid from "@/components/shared/datagrid/DataGrid";
import {CustomEmptyOverlay} from "@/components/shared/datagrid/CustomEmptyOverlay";
import React from "react";
import {
    ServiceProviderCapabilities,
    ServiceProviderDeliveries,
    ServiceProviderSubscriptions
} from "@/types/serviceProviders";
import CapabilityDrawer from "@/components/shared/drawer/CapabilityDrawer";
import {Capability, Subscription} from "@/types/neighbours";
import CommonDrawer from "@/components/shared/drawer/CommonDrawer";

type Props = {
    row: any;
    drawerOpen: boolean;
    serviceProviderRow: ServiceProviderSubscriptions | ServiceProviderDeliveries | ServiceProviderCapabilities | null;
    field: string | null;
    handleMoreClose: () => void;
    handleOnRowClick: (arg0: any) => void;
};
const nestedGridServiceProviders = ({row, field, drawerOpen, serviceProviderRow, handleMoreClose, handleOnRowClick}: Props) => {

    if (!row || !field) {
        return null;
    }

    let nestedData: object[] = [];
    let nestedColumns: GridColDef[] = [];


    if (field === "capabilities") {
        nestedData = row.capabilities.map((capability: any) => ({
            id: capability.id,
            publisherId: capability.application.publisherId,
            publicationId: capability.application.publicationId,
            messageType: capability.application.messageType,
            protocolVersion: capability.application.protocolVersion,
            originatingCountry: capability.application.originatingCountry,
            application: capability.application,
            metadata: capability.metadata,
            status: capability.status,
            shards: capability.shards,
            createdTimestamp: timeConverter(capability.createdTimestamp)
        }));

        nestedColumns = [
            {...dataGridTemplate, field: "publisherId", headerName: "Publisher ID"},
            {...dataGridTemplate, field: "publicationId", headerName: "Publication ID"},
            {
                ...dataGridTemplate, field: "messageType", headerName: "Message Type", renderCell: (cell) => {
                    return (
                        <Chip
                            color={messageTypeChips[cell.value as keyof typeof messageTypeChips] as ChipProps['color']}
                            label={cell.value}
                        />
                    );
                }
            },
            {...dataGridTemplate, field: "protocolVersion", headerName: "Protocol version"},
            {...dataGridTemplate, field: "originatingCountry", headerName: "Originating Country"},
            {...dataGridTemplate, field: "createdTimestamp", headerName: "Created"}
        ];
    } else if (field === "subscriptions") {
        nestedData = row.subscriptions.map((subscription: any) => ({
            id: subscription.id,
            status: subscription.status,
            selector: subscription.selector,
            errorMessage: subscription.errorMessage,
            consumerCommonName: subscription.consumerCommonName,
            description: subscription.description,
            endpoints: subscription.endpoints,
            lastUpdated: timeConverter(subscription.lastUpdated)
        }));

        nestedColumns = [
            {...dataGridTemplate, field: "id", headerName: "ID"},
            {
                ...dataGridTemplate, field: "status", headerName: "Status", renderCell: (cell) => {
                    return (
                        <Chip
                            color={statusChips[cell.value as keyof typeof statusChips] as ChipProps['color']}
                            label={cell.value}
                        />
                    );
                }
            },
            {...dataGridTemplate, field: "description", headerName: "Description"},
            {...dataGridTemplate, field: "lastUpdated", headerName: "Last Updated"},
        ];
    } else if (field === "deliveries") {
        nestedData = row.deliveries.map((subscription: any) => ({
            id: subscription.id,
            status: subscription.status,
            selector: subscription.selector,
            description: subscription.description,
            endpoints: subscription.endpoints,
            lastUpdatedTimestamp: timeConverter(subscription.lastUpdatedTimestamp)
        }));

        nestedColumns = [
            {...dataGridTemplate, field: "id", headerName: "ID"},
            {
                ...dataGridTemplate, field: "status", headerName: "Status", renderCell: (cell) => {
                    return (
                        <Chip
                            color={statusChips[cell.value as keyof typeof statusChips] as ChipProps['color']}
                            label={cell.value}
                        />
                    );
                }
            },
            {...dataGridTemplate, field: "description", headerName: "Description"},
            {...dataGridTemplate, field: "lastUpdatedTimestamp", headerName: "Last Updated"}
        ];
    }

    return (
        <Box flex={1}>
            <Mainheading>{field}</Mainheading>
            <Subheading>
                These are all of {field}. You can click a row to view more information.
            </Subheading>
            <Divider sx={{marginY: 3}}/>
            <Box sx={{height: 450, width: "100%"}}>
                {field === 'capabilities' && (
                    <DataGrid
                        rows={nestedData}
                        columns={nestedColumns}
                        getRowId={(row) => row.id}
                        onRowClick={handleOnRowClick}
                        sort={{field: "createdTimestamp", sort: "desc"}}
                        slots={{
                            noRowsOverlay: CustomEmptyOverlay
                        }}
                    />
                )}
                {(field === 'subscriptions' || field === 'deliveries') && (
                    <DataGrid
                        rows={nestedData}
                        columns={nestedColumns}
                        getRowId={(row) => row.id}
                        onRowClick={handleOnRowClick}
                        sort={{field: "lastUpdated", sort: "desc"}}
                        slots={{
                            noRowsOverlay: CustomEmptyOverlay
                        }}
                    />
                )}
                {serviceProviderRow && field === 'capabilities' && (
                    <CapabilityDrawer
                        handleMoreClose={handleMoreClose}
                        open={drawerOpen}
                        capabilities={serviceProviderRow as ServiceProviderCapabilities}
                    />
                )}
                {serviceProviderRow && (field === 'subscriptions' || field === 'deliveries') && (
                    <CommonDrawer
                        handleMoreClose={handleMoreClose}
                        open={drawerOpen}
                        subscriptions={serviceProviderRow as ServiceProviderSubscriptions | ServiceProviderDeliveries}
                        heading={field}
                    />
                )}
            </Box>

        </Box>
    );
}
export default nestedGridServiceProviders;