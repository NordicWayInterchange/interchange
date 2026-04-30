import {Box, Divider} from "@mui/material";
import Mainheading from "@/components/shared/typography/Mainheading";
import Subheading from "@/components/shared/typography/Subheading";

import React from "react";
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
const NestedGridConnections = ({nestedConnectionData, nestedConnectionColumns, handleOnRowClick}: Props) => {
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