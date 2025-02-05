import {Box, Divider, Typography} from "@mui/material";
import Mainheading from "@/components/shared/typography/Mainheading";
import Subheading from "@/components/shared/typography/Subheading";

import React from "react";
import DataGrid from "@/components/shared/datagrid/DataGrid";
import {CustomEmptyOverlay} from "@/components/shared/datagrid/CustomEmptyOverlay";
import {Chip} from "@/components/shared/Chip";
import {timeConverter} from "@/lib/timeConverter";


type Props = {
    row: any;
    nestedConnectionData: any;
    nestedConnectionColumns: any;
};
const NestedGridConnections = ({row, nestedConnectionData, nestedConnectionColumns}: Props) => {
    return (
        <Box flex={1}>
            <Mainheading>Connections</Mainheading>
            <Subheading>
                These are all of connections for subscription with Id:
                <Chip
                    color="orangeLight"
                    label={row?.id}
                />
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