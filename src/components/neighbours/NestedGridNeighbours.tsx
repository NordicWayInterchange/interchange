import {dataGridTemplate} from "@/components/shared/datagrid/DataGridTemplate";
import {Chip} from "@/components/shared/Chip";
import {messageTypeChips, statusChips} from "@/lib/statusChips";
import {Box, ChipProps, Divider, Typography} from "@mui/material";
import Mainheading from "@/components/shared/typography/Mainheading";
import DataGrid from "@/components/shared/datagrid/DataGrid";
import React from "react";
import {Neighbours} from "@/types/neighbours";
import CommonDrawer from "@/components/shared/layout/CommonDrawer";

type Props = {
    drawerOpen: boolean;
    neighbourRow: Neighbours | undefined ;
    field: string;
    handleMoreClose: () => void;
    handleOnRowClick: (any) => void;
};

const nestedGridNeighbours = ({row, field, drawerOpen, neighbourRow, handleMoreClose, handleOnRowClick} : Props) => {

    const nestedTableTitle = {
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
            createdTimestamp: new Date(capability.createdTimestamp).toLocaleString()
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
            consumerCommonName: subscription.consumerCommonName,
            subscriptionStatus: subscription.subscriptionStatus,
            lastUpdatedTimestamp: new Date(
                subscription.lastUpdatedTimestamp
            ).toLocaleString(),
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
            consumerCommonName: subscription.consumerCommonName,
            subscriptionStatus: subscription.subscriptionStatus,
            lastUpdatedTimestamp: new Date(
                subscription.lastUpdatedTimestamp
            ).toLocaleString(),
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
            <Mainheading>{field.split(" ").map(field => nestedTableTitle[field] || field).join(" ")}</Mainheading>
            <Divider sx={{marginY: 4}}/>
            {nestedData.length > 0 ? (
                <Box sx={{height: 300, width: "90%"}}>
                    <DataGrid
                        rows={nestedData}
                        columns={nestedColumns}
                        getRowId={(row) => row.id}
                        onRowClick={handleOnRowClick}
                        sort={{field: "lastUpdated", sort: "desc"}}
                    />
                    {neighbourRow && (
                        <CommonDrawer
                            open={drawerOpen}
                            onClose={handleMoreClose}
                        />
                    )}
                </Box>
            ) : (
                <Typography variant="body2">No data available.</Typography>
            )}
        </Box>
    );
}
export default nestedGridNeighbours;