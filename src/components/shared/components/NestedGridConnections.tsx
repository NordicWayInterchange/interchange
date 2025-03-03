import {GridColDef} from "@mui/x-data-grid";
import {timeConverter} from "@/lib/timeConverter";
import {dataGridTemplate} from "@/components/shared/datagrid/DataGridTemplate";
import {Chip} from "@/components/shared/components/Chip";
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
    drawerOpen: boolean;
    nestedConnectionData: any;
    nestedConnectionColumns: any;
    field: string | null;
    handleMoreClose: () => void;
    handleOnRowClick: (arg0: any) => void;
};
const NestedGridConnections = ({row, field, drawerOpen, nestedConnectionData, nestedConnectionColumns, handleMoreClose, handleOnRowClick}: Props) => {
    return (
        <Box flex={1}>
            <Mainheading>Connections</Mainheading>
            <Subheading>
                These are all of connections.
            </Subheading>
            <Divider sx={{marginY: 3}}/>
            <DataGrid
                rows={nestedConnectionData}
                columns={nestedConnectionColumns}
                getRowId={(row) => row.id}
                onRowClick={handleOnRowClick}
                sort={{field: "createdTimestamp", sort: "desc"}}
                slots={{
                    noRowsOverlay: CustomEmptyOverlay
                }}
            />
        </Box>
    );
}
export default NestedGridConnections;