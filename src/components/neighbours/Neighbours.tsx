import React, {useState} from 'react';
import { DataGrid, GridColDef, GridRowsProp } from "@mui/x-data-grid";
import {Box, Divider} from "@mui/material";
import Mainheading from "@/components/shared/typography/Mainheading";
import {useFetchNeighbours} from "@/hooks/useFetchNeighbours";
import {useSession} from "next-auth/react";

const columns: GridColDef[] = [
    { field: "id", headerName: "ID", width: 90 },
    { field: "name", headerName: "Name", width: 150 },
];

const nestedColumns: GridColDef[] = [
    { field: "id", headerName: "Detail ID", width: 120 },
    { field: "email", headerName: "Email", width: 200 },
    { field: "age", headerName: "Age", type: "number", width: 100 },
];

const rows: GridRowsProp = [
    {
        id: 1,
        name: "Alice",
        details: [
            { id: 101, email: "alice1@example.com", age: 24 },
            { id: 102, email: "alice2@example.com", age: 25 }
        ]
    },
    {
        id: 2,
        name: "Bob",
        details: [
            { id: 201, email: "bob1@example.com", age: 30 },
            { id: 202, email: "bob2@example.com", age: 32 }
        ]
    }
];

const Neighbours: React.FC = () => {
    const { data: session } = useSession();

    const { data, isLoading } = useFetchNeighbours(
        session?.user.commonName as string
    );

    const [expandedRow, setExpandedRow] = useState<number | null>(null);

    const handleRowClick = (id: number) => {
        setExpandedRow((prevExpandedRow) => (prevExpandedRow === id ? null : id));
    };
    console.log('data', data)
    return (
        <Box sx={{ height: 400, width: "100%" }}>
            <Mainheading>Neighbours</Mainheading>
            <Divider sx={{ marginY: 4 }} />
            <DataGrid
                rows={rows}
                columns={columns}
                pageSize={5}
                disableSelectionOnClick
                onRowClick={(params) => handleRowClick(params.row.id)}
                getRowId={(row) => row.id} // Set row id
            />
            {expandedRow !== null && (
                <Box sx={{ marginTop: 2 }}>
                    {rows
                        .filter((row) => row.id === expandedRow)
                        .map((row) => (
                            <Box key={row.id} sx={{ marginBottom: 2 }}>
                                <DataGrid
                                    rows={row.details}
                                    columns={nestedColumns}
                                    pageSize={5}
                                    disableSelectionOnClick
                                    hideFooter
                                />
                            </Box>
                        ))}
                </Box>
            )}
        </Box>
    );
};

export default Neighbours;
