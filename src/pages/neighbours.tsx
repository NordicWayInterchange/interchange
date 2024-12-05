import React, {useState} from 'react';
import {DataGrid, GridColDef} from "@mui/x-data-grid";
import {useFetchNeighbours} from "@/hooks/useFetchNeighbours";
import {useSession} from "next-auth/react";
import Mainheading from "@/components/shared/typography/Mainheading";
import {Box} from "@mui/material";
import {Chip} from "@/components/shared/Chip";
import {connextionStatusChips} from "@/lib/statusChips";


const Neighbours: React.FC = () => {
    const { data: session } = useSession();

    const { data, isLoading } = useFetchNeighbours(
        session?.user.commonName as string
    );

    const [expandedRows, setExpandedRows] = useState({});

    /*const handleCellClick = (row, field) => {
        console.log(row);
        console.log(field);
        setExpandedRows((prev) => ({
            ...prev,
            [row.id]: prev[row.id] === field ? null : field, // Toggle based on field
        }));
    };*/

    console.log(data);
    const tableHeaders: GridColDef[] = [
        {
            field: "neighbour_id",
            headerName: "ID",
            flex: 1,
        },
        {
            field: "name",
            headerName: "Name",
            flex: 1
        },
        {
            field: "capabilities",
            headerName: "Capabilities",
            flex: 1,
            renderCell: (params) => {
                const value = params.row.capabilities.capabilities;
                return Array.isArray(value) ? value.length : 0;
            },
        },
        {
            field: "ourRequestedSubscriptions",
            headerName: "Our Subscriptions",
            flex: 1,
            renderCell: (params) => (
                <span
                    style={{cursor: "pointer"}}
                    onClick={() => handleCellClick(params.row, "ourRequestedSubscriptions")}
                >
            {Array.isArray(params.row.ourRequestedSubscriptions.subscriptions) ? params.row.ourRequestedSubscriptions.subscriptions.length : 0}
        </span>
            ),
        },
        {
            field: "neighbourRequestedSubscriptions",
            headerName: "Neighbour Subscriptions",
            flex: 1,
            renderCell: (params) => (
                <span
                    style={{cursor: "pointer"}}
                    onClick={() => handleCellClick(params.row, "neighbourRequestedSubscriptions")}
                >
            {Array.isArray(params.row.neighbourRequestedSubscriptions.subscriptions) ? params.row.neighbourRequestedSubscriptions.subscriptions.length : 0}

        </span>
            ),
        },
        {
            field: "connectionStatus",
            headerName: "Connection Status",
            flex : 1,
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

    //const [selectedDetail, setSelectedDetail] = useState(null);

    /*const handleCellClick = (row, field) => {
        setSelectedDetail({field, data: row.ourRequestedSubscriptions.subscription});
    };*/
    return (
        <Box flex={1}>
            <Mainheading>Subscriptions</Mainheading>
        <div style={{height: 400, width: "100%"}}>
            <DataGrid
                columns={tableHeaders}
                rows={data || []}
                loading={isLoading}
                getRowId={(row) => row.neighbour_id}
                sort={{ field: "lastUpdated", sort: "desc" }}
            />

        </div>
        </Box>
    );
};

export default Neighbours;
