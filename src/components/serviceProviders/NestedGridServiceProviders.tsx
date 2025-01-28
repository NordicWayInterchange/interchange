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
import CapabilityDrawer from "@/components/shared/drawer/CapabilityDrawer";
import {Capability, Subscription} from "@/types/neighbours";
import OurAndNeighbourSubscriptionDrawer from "@/components/shared/drawer/OurAndNeighbourSubscriptionDrawer";
import React from "react";
import {
    ServiceProviderCapabilities,
    ServiceProviderDeliveries,
    ServiceProviderSubscriptions
} from "@/types/serviceProviders";

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
            messageType: capability.application.messageType,
            originatingCountry: capability.application.originatingCountry,
            application: capability.application,
            metadata: capability.metadata,
            createdTimestamp: timeConverter(capability.createdTimestamp)
        }));

        nestedColumns = [
            {...dataGridTemplate, field: "id", headerName: "ID"},
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
            {...dataGridTemplate, field: "originatingCountry", headerName: "Originating Country"},
            {...dataGridTemplate, field: "createdTimestamp", headerName: "Created"}
        ];
    } else if (field === "subscriptions") {
        nestedData = row.subscriptions.map((subscription: any) => ({
            id: subscription.subreq_id,
            subscriptionStatus: subscription.subscriptionStatus,
            selector: subscription.selector,
            path: subscription.path,
            consumerCommonName: subscription.consumerCommonName,
            endpoints: subscription.endpoints,
            lastUpdatedTimestamp: timeConverter(subscription.lastUpdatedTimestamp)
        }));

        nestedColumns = [
            {...dataGridTemplate, field: "id", headerName: "ID"},
            {...dataGridTemplate, field: "consumerCommonName", headerName: "Consumer Common Name"},
            {
                ...dataGridTemplate, field: "subscriptionStatus", headerName: "Status", renderCell: (cell) => {
                    return (
                        <Chip
                            color={statusChips[cell.value as keyof typeof statusChips] as ChipProps['color']}
                            label={cell.value}
                        />
                    );
                }
            },
            {
                ...dataGridTemplate,
                field: "lastUpdatedTimestamp",
                headerName: "Last Updated",
            },
        ];
    } else if (field === "deliveries") {
        nestedData = row.deliveries.map((subscription: any) => ({
            id: subscription.id,
            subscriptionStatus: subscription.subscriptionStatus,
            selector: subscription.selector,
            path: subscription.path,
            consumerCommonName: subscription.consumerCommonName,
            endpoints: subscription.endpoints,
            lastUpdatedTimestamp: timeConverter(subscription.lastUpdatedTimestamp)
        }));

        nestedColumns = [
            {...dataGridTemplate, field: "id", headerName: "ID"},
            {...dataGridTemplate, field: "consumerCommonName", headerName: "Consumer Common Name"},
            {
                ...dataGridTemplate, field: "subscriptionStatus", headerName: "Status", renderCell: (cell) => {
                    return (
                        <Chip
                            color={statusChips[cell.value as keyof typeof statusChips] as ChipProps['color']}
                            label={cell.value}
                        />
                    );
                }
            },
            {
                ...dataGridTemplate,
                field: "lastUpdatedTimestamp",
                headerName: "Last Updated",
            },
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
                {field === 'Capabilities' && (
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
            </Box>

        </Box>
    );
}
export default nestedGridServiceProviders;