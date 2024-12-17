import {dataGridTemplate} from "@/components/shared/datagrid/DataGridTemplate";
import {Chip} from "@/components/shared/Chip";
import {messageTypeChips, statusChips} from "@/lib/statusChips";
import {Box, ChipProps, Divider} from "@mui/material";
import Mainheading from "@/components/shared/typography/Mainheading";
import DataGrid from "@/components/shared/datagrid/DataGrid";
import React from "react";
import {Neighbours} from "@/types/neighbours";
import Subheading from "@/components/shared/typography/Subheading";
import CapabilityDrawer from "@/components/shared/drawer/CapabilityDrawer";
import OurAndNeighbourSubscriptionDrawer from "@/components/shared/drawer/OurAndNeighbourSubscriptionDrawer";
import {CustomEmptyOverlay} from "@/components/shared/datagrid/CustomEmptyOverlay";
import {timeConverter} from "@/lib/timeConverter";

type Props = {
    drawerOpen: boolean;
    neighbourRow: Neighbours | undefined;
    field: string;
    handleMoreClose: () => void;
    handleOnRowClick: (any) => void;
};

const nestedGridNeighbours = ({row, field, drawerOpen, neighbourRow, handleMoreClose, handleOnRowClick}: Props) => {

    const nestedTableTitle = {
        capabilities: "Capabilities",
        ourRequestedSubscriptions: "Our Subscriptions",
        neighbourRequestedSubscriptions: "Neighbour Subscriptions",
    };

    if (!row || !field) {
        return null;
    }

    let nestedData = [];
    let nestedColumns = [];

    if (field === "capabilities") {
        nestedData = row.capabilities.capabilities.map((capability) => ({
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
            {...dataGridTemplate, field: "createdTimestamp", headerName: "Last updated"}
        ];
    } else if (field === "neighbourRequestedSubscriptions") {
        nestedData = row.neighbourRequestedSubscriptions.subscriptions.map((subscription) => ({
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
    } else if (field === "ourRequestedSubscriptions") {
        nestedData = row.ourRequestedSubscriptions.subscriptions.map((subscription) => ({
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

    const heading = field.split(" ").map(field => nestedTableTitle[field] || field).join(" ");
    return (
        <Box flex={1}>
            <Mainheading>{heading}</Mainheading>
            <Subheading>
                These are all of {heading}. You can click a row to view more information.
            </Subheading>
            <Divider sx={{marginY: 3}}/>
            <Box sx={{height: 300, width: "100%"}}>
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
                {neighbourRow && heading === 'Capabilities' && (
                    <CapabilityDrawer
                        handleMoreClose={handleMoreClose}
                        open={drawerOpen}
                        capabilities={neighbourRow}
                    />
                )}
                {neighbourRow && (heading === 'Our Subscriptions' || heading === 'Neighbour Subscriptions') && (
                    <OurAndNeighbourSubscriptionDrawer
                        handleMoreClose={handleMoreClose}
                        open={drawerOpen}
                        subscriptions={neighbourRow}
                        heading={heading}
                    />
                )}
            </Box>

        </Box>
    );
}
export default nestedGridNeighbours;