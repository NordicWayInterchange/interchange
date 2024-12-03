import React from 'react';
import { DataGrid, GridColDef, GridRowsProp } from "@mui/x-data-grid";
import {Box} from "@mui/material";

const columns: GridColDef[] = [
    { field: "id", headerName: "ID", width: 70 },
    { field: "name", headerName: "Name", width: 130 },
    { field: "age", headerName: "Age", type: "number", width: 90 },
    {
        field: "email",
        headerName: "Email",
        width: 200,
        sortable: false,
    },
    {
        field: "isAdmin",
        headerName: "Admin",
        width: 120,
        type: "boolean",
        renderCell: (params) => (params.value ? "Yes" : "No"), // Custom cell rendering
    },
];

const rows: GridRowsProp = [
    { id: 1, name: "Alice", age: 25, email: "alice@example.com", isAdmin: true },
    { id: 2, name: "Bob", age: 30, email: "bob@example.com", isAdmin: false },
    { id: 3, name: "Charlie", age: 35, email: "charlie@example.com", isAdmin: true },
    { id: 4, name: "Dave", age: 40, email: "dave@example.com", isAdmin: false },
];

const Neighbours: React.FC = () => {
    return (
        <Box
            sx={{
                height: 300,
                width: "100%",
                backgroundColor: "menuBackgroundColor",
                color: "white",
            }}
        >
            <DataGrid
                rows={rows}
                columns={columns}
                pageSize={5}
                sx={{
                    ".MuiDataGrid-root": {
                        backgroundColor: 'mainBackgroundColor',
                    },
                    ".MuiDataGrid-cell": {
                        color: 'white',
                    },
                    ".MuiDataGrid-columnHeaders": {
                        backgroundColor: "#333",
                        color: 'white',
                    },
                    ".MuiDataGrid-footerContainer": {
                        backgroundColor: "#333",
                        color: 'white',
                    },
                }}
            />
        </Box>
    );
};

export default Neighbours;
