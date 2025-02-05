import {Box, Divider, Typography} from "@mui/material";
import Mainheading from "@/components/shared/typography/Mainheading";
import Subheading from "@/components/shared/typography/Subheading";

import React from "react";
import DataGrid from "@/components/shared/datagrid/DataGrid";
import {CustomEmptyOverlay} from "@/components/shared/datagrid/CustomEmptyOverlay";
import {Chip} from "@/components/shared/Chip";
import { motion } from "framer-motion";


type Props = {
    row: any;
    nestedConnectionData: any;
    nestedConnectionColumns: any;
    isFlashing: boolean;
};
const NestedGridConnections = ({row, nestedConnectionData, nestedConnectionColumns, isFlashing}: Props) => {
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
            <motion.div
                animate={{backgroundColor: isFlashing ? "#ffbf7d" : "#f0f1f1"}}
                transition={{duration: 0.3, ease: "easeInOut"}}
                style={{padding: "5px", borderRadius: "8px"}}
            >
                <DataGrid
                    rows={nestedConnectionData}
                    columns={nestedConnectionColumns}
                    getRowId={(row) => row.id}
                    sort={{field: "createdTimestamp", sort: "desc"}}
                    slots={{
                        noRowsOverlay: CustomEmptyOverlay
                    }}
                />
            </motion.div>
        </Box>
);
}
export default NestedGridConnections;