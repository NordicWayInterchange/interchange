import React, {useState} from 'react';
import {GridColDef} from "@mui/x-data-grid";
import {useFetchNeighbours} from "@/hooks/useFetchNeighbours";
import {useSession} from "next-auth/react";
import Mainheading from "@/components/shared/typography/Mainheading";
import {Box, Divider, Typography} from "@mui/material";
import {Chip} from "@/components/shared/Chip";
import {connextionStatusChips} from "@/lib/statusChips";
import DataGrid from "@/components/shared/datagrid/DataGrid";
import {dataGridTemplate} from "@/components/shared/datagrid/DataGridTemplate";


const Neighbours: React.FC = () => {
    const { data: session } = useSession();

    const { data, isLoading } = useFetchNeighbours(
        session?.user.commonName as string
    );

    const [expandedRows, setExpandedRows] = useState({});

    const handleCellClick = (row, field) => {
        console.log('Rowwww', row)
        const rowId = row.id ? row.id : row.subreq_id;
        setExpandedRows((prev) => ({
            ...prev,
            [rowId]: prev[rowId] === field ? null : field,
        }));
    };

    console.log(data);
    const tableHeaders: GridColDef[] = [
        {
            ...dataGridTemplate,
            field: "neighbour_id",
            headerName: "ID"
        },
        {
            ...dataGridTemplate,
            field: "name",
            headerName: "Name"
        },
        {
            ...dataGridTemplate,
            field: "capabilities",
            headerName: "Capabilities",
            renderCell: (params) => (
                <span
                    style={{cursor: "pointer"}}
                    onClick={() => handleCellClick(params.row.capabilities, "capabilities")}
                >
            {Array.isArray(params.row.capabilities.capabilities) ? params.row.capabilities.capabilities.length : 0}
        </span>
            ),
        },
        {
            ...dataGridTemplate,
            field: "ourRequestedSubscriptions",
            headerName: "Our Subscriptions",
            renderCell: (params) => (
                <span
                    style={{cursor: "pointer"}}
                    onClick={() => handleCellClick(params.row.ourRequestedSubscriptions, "ourRequestedSubscriptions")}
                >
            {Array.isArray(params.row.ourRequestedSubscriptions.subscriptions) ? params.row.ourRequestedSubscriptions.subscriptions.length : 0}
        </span>
            ),
        },
        {
            ...dataGridTemplate,
            field: "neighbourRequestedSubscriptions",
            headerName: "Neighbour Subscriptions",
            renderCell: (params) => (
                <span
                    style={{cursor: "pointer"}}
                    onClick={() => handleCellClick(params.row.neighbourRequestedSubscriptions, "neighbourRequestedSubscriptions")}
                >
            {Array.isArray(params.row.neighbourRequestedSubscriptions.subscriptions) ? params.row.neighbourRequestedSubscriptions.subscriptions.length : 0}

        </span>
            ),
        },
        {
            ...dataGridTemplate,
            field: "connectionStatus",
            headerName: "Connection Status",
            renderCell: (cell) => {
                return (
                    <Chip
                        color={
                            connextionStatusChips[
                                cell.value as keyof typeof connextionStatusChips
                                ] as any
                        }
                        label={cell.value}
                    />
                );
            },
        },
    ];

    const renderNestedTable = (row, field) => {
        if (!row || !field) {
            console.error("Invalid row or field:", row, field);
            return null;
        }

        let nestedData = [];
        let nestedColumns = [];

        if (field === "capabilities") {
            nestedData = row.capabilities.capabilities.map((capability) => ({
                id: capability.id,
                messageType: capability.application.messageType,
                originatingCountry: capability.application.originatingCountry
            }));

            nestedColumns = [
                { ...dataGridTemplate, field: "id", headerName: "ID" },
                { ...dataGridTemplate, field: "messageType", headerName: "Message Type" },
                { ...dataGridTemplate, field: "originatingCountry", headerName: "Originating Country" }
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
                { ...dataGridTemplate, field: "id", headerName: "ID" },
                { ...dataGridTemplate, field: "consumerCommonName", headerName: "Consumer Common Name" },
                { ...dataGridTemplate, field: "subscriptionStatus", headerName: "Status"},
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
                { ...dataGridTemplate, field: "id", headerName: "ID" },
                { ...dataGridTemplate, field: "consumerCommonName", headerName: "Consumer Common Name" },
                { ...dataGridTemplate, field: "subscriptionStatus", headerName: "Status" },
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
                <Divider sx={{ marginY: 4 }} />
                {nestedData.length > 0 ? (
                    <Box sx={{ height: 300, width: "90%" }}>
                        <DataGrid
                            rows={nestedData}
                            columns={nestedColumns}
                            getRowId={(row) => row.id}
                            sort={{ field: "lastUpdated", sort: "desc" }}
                        />
                    </Box>
                ) : (
                    <Typography variant="body2">No data available.</Typography>
                )}
            </Box>
        );
    };

    return (
        <Box flex={1}>
        <Mainheading>Subscriptions</Mainheading>
            <Divider sx={{ marginY: 4 }} />
            <Box sx={{ height: 400, width: "100%" }}>
                <DataGrid
                    columns={tableHeaders}
                    rows={data || []}
                    loading={isLoading}
                    getRowId={(row) => row.neighbour_id}
                    sort={{ field: "lastUpdated", sort: "desc" }}
                />
            </Box>
            {Object.keys(expandedRows).map((rowId) => {
                console.log('rowId', rowId);
                const row = Array.isArray(data) ? data.find((item) => item.neighbour_id === parseInt(rowId)) : null;
                const field = expandedRows[rowId];

                console.log("Row:", row, "Field:", field);
                if (!row) {
                    console.error(`Row with neighbour_id ${rowId} not found in data.`);
                    return null;
                }

                return (
                    <Box key={rowId}>
                        {renderNestedTable(row, field)}
                    </Box>
                );
            })}
        </Box>
    );
};

export default Neighbours;
