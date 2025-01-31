import {GridColDef} from "@mui/x-data-grid";
import {timeConverter} from "@/lib/timeConverter";
import {dataGridTemplate} from "@/components/shared/datagrid/DataGridTemplate";
import {Chip} from "@/components/shared/Chip";
import {messageTypeChips, statusChips} from "@/lib/statusChips";
import {Box, ChipProps, Divider} from "@mui/material";
import Mainheading from "@/components/shared/typography/Mainheading";
import Subheading from "@/components/shared/typography/Subheading";

import React, {useState} from "react";
import {
    Connection,
} from "@/types/serviceProviders";
import DataGrid from "@/components/shared/datagrid/DataGrid";
import {CustomEmptyOverlay} from "@/components/shared/datagrid/CustomEmptyOverlay";


type Props = {
    row: any;
    nestedConnectionData: any;
    nestedConnectionColumns: any;
};
const NestedGridConnections = ({row, nestedConnectionData, nestedConnectionColumns}: Props) => {
    console.log('row', row)
    return (
        <Box flex={1}>
            <Mainheading>Connections</Mainheading>
            <Subheading>
                These are all of connections for subscription with Id: {row.id}
            </Subheading>
            <Divider sx={{marginY: 3}}/>
            <DataGrid
                rows={nestedConnectionData}
                columns={nestedConnectionColumns}
                getRowId={(row) => row.id}
                sort={{field: "createdTimestamp", sort: "desc"}}
                slots={{
                    noRowsOverlay: CustomEmptyOverlay
                }}
            />
        </Box>
    );
}
export default NestedGridConnections;