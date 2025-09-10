import React, {useState} from 'react';
import {GridColDef} from "@mui/x-data-grid";
import {useSession} from "next-auth/react";
import Mainheading from "@/components/shared/typography/Mainheading";
import {Box, Divider} from "@mui/material";
import DataGrid from "@/components/shared/datagrid/DataGrid";
import {dataGridTemplate} from "@/components/shared/datagrid/DataGridTemplate";
import Subheading from "@/components/shared/typography/Subheading";
import {
    CustomEmptyOverlayExchanges,
} from "@/components/shared/datagrid/CustomEmptyOverlay";
import { StyledTableHeader} from "@/components/styles/StyledElements";
import {useFetchQueues} from "@/hooks/useFetchQueues";
import SearchBox from "@/components/shared/components/SearchBox";


const Queues = () => {
    const {data: session} = useSession();

    const {data: queuesData, isLoading} = useFetchQueues(
        session?.user.commonName as string
    );
    const [searchName, setSearchName] = useState("");


    const tableHeaders: GridColDef[] = [
        {
            ...dataGridTemplate,
            field: "id",
            headerName: "ID",
            flex: 2
        },
        {
            ...dataGridTemplate,
            field: "name",
            headerName: "Name",
            flex: 2
        },
        {
            ...dataGridTemplate,
            field: "durable",
            headerName: "Durable",
            flex: 1
        },
        {
            ...dataGridTemplate,
            field: "maximumMessageTtl",
            headerName: "Maximum Message Ttl",
            flex: 1
        },
        {
            ...dataGridTemplate,
            field: "ensureNondestructiveConsumers",
            headerName: "Ensure Nondestructive Consumers",
            flex: 2
        }
    ];

    const rows = Array.isArray(queuesData) ? queuesData : [];

    console.log(searchName)
    const filteredRows = searchName.trim()
        ? rows.filter((row) =>
            row.name?.toString().includes(searchName.trim())
        )
        : rows;

    return (
        <Box flex={1}>
            <Mainheading>Queues</Mainheading>
            <Subheading>
                These are all of qpid queues.
            </Subheading>
            <Divider sx={{marginY: 2}}/>
            <SearchBox searchId={searchName} setSearchId={setSearchName} label="a queue" searchElement="name"/>
            <Divider style={{ margin: '8px 0', visibility: 'hidden' }}/>
            <Box sx={{height: 450, width: "100%"}}>
                <Box sx={StyledTableHeader}>
                    <DataGrid
                        columns={tableHeaders}
                        rows={filteredRows || []}
                        loading={isLoading}
                        getRowId={(row) => row.id}
                        sort={{field: "id", sort: "desc"}}
                        slots={{
                            noRowsOverlay: CustomEmptyOverlayExchanges
                        }}
                        />

                </Box>
            </Box>
        </Box>
    );
};
export default Queues;
