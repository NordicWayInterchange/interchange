import {GridColDef} from "@mui/x-data-grid";
import {dataGridTemplate} from "@/components/shared/datagrid/DataGridTemplate";
import {Box, Divider} from "@mui/material";
import Mainheading from "@/components/shared/typography/Mainheading";
import Subheading from "@/components/shared/typography/Subheading";
import DataGrid from "@/components/shared/datagrid/DataGrid";
import {CustomEmptyOverlay} from "@/components/shared/datagrid/CustomEmptyOverlay";
import React from "react";
import {StyledTableHeader} from "@/components/styles/StyledElements";
import {motion} from "framer-motion";
import {Chip} from "@/components/shared/components/Chip";


type Props = {
    nestedBindingData: any;
    field: string | null;
    isFlashing: boolean;
};
const NestedGridExchanges: React.FC<Props> = ({
                                                  nestedBindingData,
                                                  field,
                                                  isFlashing
                                              }: Props) => {

    if (!nestedBindingData || !field) {
        return null;
    }

    let nestedData: object[] = [];
    let nestedColumns: GridColDef[] = [];

    nestedData = nestedBindingData.bindings.map((binding: any, index: number) => ({
        bindingKey: binding.bindingKey,
        destination: binding.destination,
        "x-filter-jms-selector": binding.arguments["x-filter-jms-selector"],
        uniqueId: `${binding.bindingKey}-${index}`
    }));

    nestedColumns = [
        {
            ...dataGridTemplate, field: "destination", headerName: "Destination", renderCell: (params) => {
                return params.row.destination;
            },
        },
        {
            ...dataGridTemplate, field: "bindingKey", headerName: "bindingKey", renderCell: (params) => {
                return params.row.bindingKey;
            }
        },
        {
            ...dataGridTemplate, field: "argmennts", headerName: "Arguments", flex: 2.7, minWidth: 400,
            renderCell: (params) => (
                <div
                    style={{
                        whiteSpace: 'normal',
                        wordBreak: 'break-word',
                        overflowWrap: 'break-word',
                        lineHeight: 2,
                        alignItems: 'start'
                    }}
                >
                    {params.row?.["x-filter-jms-selector"]}
                </div>
            ),
        },
    ];


    return (
        <Box sx={{flex:1}}>
            <motion.div
                animate={{backgroundColor: isFlashing ? "#ffdbb0" : "#f0f1f1"}}
                transition={{duration: 0.3, ease: "easeInOut"}}
                style={{padding: "5px", borderRadius: "8px"}}
            >

                <Divider style={{ margin: '20px 0', visibility: 'hidden' }}/>
                <Mainheading>Bindings</Mainheading>
                <Subheading>
                    These are all of bindings for exchange with id:
                    <Chip
                        label={field}
                        sx={{backgroundColor: "#ffbf7d", color: "black"}}
                    />
                </Subheading>
                <Divider sx={{marginY: 3}}/>
                <Box sx={{height: 450, width: "100%"}}>
                    <Box sx={StyledTableHeader}>

                        <DataGrid
                            rows={nestedData}
                            columns={nestedColumns}
                            getRowId={(row) => row.uniqueId}
                            sort={{field: "bindingKey", sort: "desc"}}
                            slots={{
                                noRowsOverlay: CustomEmptyOverlay
                            }}
                            rowHeight={80}
                        />
                    </Box>
                </Box>
            </motion.div>
        </Box>
    );
}
export default NestedGridExchanges;