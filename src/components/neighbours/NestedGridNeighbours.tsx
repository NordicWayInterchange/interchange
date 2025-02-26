import {dataGridTemplate} from "@/components/shared/datagrid/DataGridTemplate";
import {Chip} from "@/components/shared/Chip";
import {messageTypeChips, statusChips} from "@/lib/statusChips";
import {Box, ChipProps, Divider} from "@mui/material";
import Mainheading from "@/components/shared/typography/Mainheading";
import DataGrid from "@/components/shared/datagrid/DataGrid";
import React from "react";
import {Capability, Subscription} from "@/types/neighbours";
import Subheading from "@/components/shared/typography/Subheading";
import CapabilityDrawer from "@/components/shared/drawer/CapabilityDrawer";
import CommonDrawer from "@/components/shared/drawer/CommonDrawer";
import {CustomEmptyOverlay} from "@/components/shared/datagrid/CustomEmptyOverlay";
import {timeConverter} from "@/lib/timeConverter";
import {GridColDef} from "@mui/x-data-grid";
import { motion } from "framer-motion";

type Props = {
    row: any;
    drawerOpen: boolean;
    neighbourRow: Capability | Subscription | null;
    field: string | null;
    handleMoreClose: () => void;
    handleOnRowClick: (arg0: any) => void;
    isFlashing: boolean;
};

function extractedSubscriptionAttributes(subscription: any) {
    return {
        id: subscription.id ? subscription.id : subscription.subreq_id,
        subscriptionStatus: subscription.subscriptionStatus,
        selector: subscription.selector,
        path: subscription.path,
        consumerCommonName: subscription.consumerCommonName,
        endpoints: subscription.endpoints,
        lastUpdatedTimestamp: timeConverter(subscription.lastUpdatedTimestamp)
    };
}

const NestedGridNeighbours : React.FC<Props> = ({row, field, drawerOpen, neighbourRow, handleMoreClose, handleOnRowClick, isFlashing}: Props) => {

    const nestedTableTitle: { [key: string]: string } = {
        capabilities: "Capabilities",
        ourRequestedSubscriptions: "Our Subscriptions",
        neighbourRequestedSubscriptions: "Neighbour Subscriptions",
    };

    if (!row || !field) {
        return null;
    }

    let nestedData: object[] = [];
    let nestedColumns: GridColDef[] = [];


    if (field === "capabilities") {
        nestedData = row.capabilities.capabilities.map((capability: any) => ({
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
    } else if (field === "neighbourRequestedSubscriptions") {
        nestedData = row.neighbourRequestedSubscriptions.subscriptions.map((subscription: any) => extractedSubscriptionAttributes(subscription));

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
        nestedData = row.ourRequestedSubscriptions.subscriptions.map((subscription: any) => extractedSubscriptionAttributes(subscription));

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
    const getSubheading = () => {
        if (heading === 'Capabilities') {
            return (
                <Box>
                    These are all of Capabilities with last capability exchange
                    <Chip
                        label={timeConverter(row.capabilities.lastCapabilityExchange)}
                        sx={{ backgroundColor: "#ffbf7d", color: "black" }}
                    />
                    . You can click a row to view more information.
                </Box>
        );
        } else return `These are all of ${heading}. You can click a row to view more information.`;
    }

    return (
        <Box flex={1}>
            <Mainheading>{heading}</Mainheading>
            <Subheading>
                {getSubheading()}
            </Subheading>
            <Divider sx={{marginY: 3}}/>
            <Box sx={{height: 450, width: "100%"}}>
                <motion.div
                    animate={{backgroundColor: isFlashing ? "#ffbf7d" : "#f0f1f1"}}
                    transition={{duration: 0.3, ease: "easeInOut"}}
                    style={{padding: "5px", borderRadius: "8px"}}
                >
                    {heading === 'Capabilities' && (
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
                    {(heading === 'Our Subscriptions' || heading === 'Neighbour Subscriptions') && (
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
                </motion.div>
                {neighbourRow && heading === 'Capabilities' && (
                        <CapabilityDrawer
                            handleMoreClose={handleMoreClose}
                            open={drawerOpen}
                            capabilities={neighbourRow as Capability}
                        />
                    )}
                    {neighbourRow && (heading === 'Our Subscriptions' || heading === 'Neighbour Subscriptions') && (
                        <CommonDrawer
                            handleMoreClose={handleMoreClose}
                            open={drawerOpen}
                            commonAttributes={neighbourRow as Subscription}
                            heading={heading}
                        />
                    )}
            </Box>

        </Box>
);
}
export default NestedGridNeighbours;