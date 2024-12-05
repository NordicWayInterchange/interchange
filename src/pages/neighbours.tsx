import React, {useState} from 'react';
import {DataGrid, GridColDef} from "@mui/x-data-grid";
import {useFetchNeighbours} from "@/hooks/useFetchNeighbours";
import {useSession} from "next-auth/react";
import Mainheading from "@/components/shared/typography/Mainheading";
import {Box} from "@mui/material";


const Neighbours: React.FC = () => {
    const { data: session } = useSession();

    const { data, isLoading } = useFetchNeighbours(
        session?.user.commonName as string
    );

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
    ];

    const [selectedDetail, setSelectedDetail] = useState(null);

    const handleCellClick = (row, field) => {
        setSelectedDetail({field, data: row.ourRequestedSubscriptions.subscription});
    };
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
            {selectedDetail && (
                <div style={{marginTop: 20}}>
                    <h3>Details for {selectedDetail.field}</h3>
                    <DataGrid
                        columns={tableHeaders}
                        rows={data || []}
                        loading={isLoading}
                        getRowId={(row) => row.ourRequestedSubscriptions.subscription.id}
                        sort={{ field: "lastUpdatedTimestamp", sort: "desc" }}
                    />
                </div>
            )}
        </div>
        </Box>
    );
};

export default Neighbours;
