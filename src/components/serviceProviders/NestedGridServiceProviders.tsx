import {GridColDef} from "@mui/x-data-grid";
import {timeConverter} from "@/lib/timeConverter";
import {dataGridTemplate} from "@/components/shared/datagrid/DataGridTemplate";
import {Chip} from "@/components/shared/components/Chip";
import {messageTypeChips, statusChips} from "@/lib/statusChips";
import {Box, ChipProps, Divider} from "@mui/material";
import Mainheading from "@/components/shared/typography/Mainheading";
import Subheading from "@/components/shared/typography/Subheading";
import DataGrid from "@/components/shared/datagrid/DataGrid";
import {CustomEmptyOverlay} from "@/components/shared/datagrid/CustomEmptyOverlay";
import React, {useState} from "react";
import {
    ServiceProviderCapabilities,
    ServiceProviderDeliveries, ServiceProviderPrivateChannels,
    ServiceProviderSubscriptions
} from "@/types/serviceProviders";
import CapabilityDrawer from "@/components/shared/drawer/CapabilityDrawer";
import CommonDrawer from "@/components/shared/drawer/CommonDrawer";
import {StyledBorderlineSpan, StyledTableHeader} from "@/components/styles/StyledElements";
import {ExpandedRows} from "@/types/expandedRows";
import NestedGridConnections from "@/components/serviceProviders/NestedGridServiceProvidedConnections";
import { motion } from "framer-motion";
import PrivateChannelDrawer from "@/components/shared/drawer/PrivateChannelDrawer";
import {useSession} from "next-auth/react";
import {useFetchDeliveryEndpoints} from "@/hooks/useFetchDeliveryEndpoints";

type Props = {
    row: any;
    drawerOpen: boolean;
    serviceProviderRow: ServiceProviderSubscriptions | ServiceProviderDeliveries | ServiceProviderCapabilities | ServiceProviderPrivateChannels | null;
    field: string | null;
    handleMoreClose: () => void;
    handleOnRowClick: (arg0: any) => void;
    isFlashing: boolean;
};
const NestedGridServiceProviders: React.FC<Props> = ({
                                                         row,
                                                         field,
                                                         drawerOpen,
                                                         serviceProviderRow,
                                                         handleMoreClose,
                                                         handleOnRowClick,
                                                         isFlashing
                                                     }: Props) => {

    const [expandedRows, setExpandedRows] = useState<ExpandedRows>({});
    const [highlightedCell, setHighlightedCell] = useState<{
        id: number | null;
        field: string | null;
    }>({id: null, field: null});

    const shouldLoud = field === "deliveries";
    console.log('field', field);
    console.log('shouldLoud', shouldLoud);
    const {data: session} = useSession();
    const { data: deliveryEndpoints } = useFetchDeliveryEndpoints(session?.user.commonName as string, shouldLoud);
    console.log('serviceProviderRow', serviceProviderRow)
    const handleCellClick = (row: any, field: any, rowId: number) => {
        setExpandedRows({});
        setExpandedRows((prev) => ({
            ...prev,
            [rowId]: prev[rowId] === field ? null : field,
        }));
    };
    if (!row || !field) {
        return null;
    }

    let nestedData: object[] = [];
    let nestedConnectionData: object[] = [];
    let nestedColumns: GridColDef[] = [];
    let nestedConnectionColumns: GridColDef[] = [];

    if (field === "capabilities") {
        nestedData = row.capabilities.map((capability: any) => ({
            id: capability.id,
            publisherId: capability.application.publisherId,
            publicationId: capability.application.publicationId,
            messageType: capability.application.messageType,
            protocolVersion: capability.application.protocolVersion,
            originatingCountry: capability.application.originatingCountry,
            application: capability.application,
            metadata: capability.metadata,
            status: capability.status,
            shards: capability.shards,
            createdTimestamp: timeConverter(capability.createdTimestamp)
        }));

        nestedColumns = [
            {...dataGridTemplate, field: "publisherId", headerName: "Publisher ID"},
            {...dataGridTemplate, field: "publicationId", headerName: "Publication ID"},
            {
                ...dataGridTemplate, field: "messageType", headerName: "Message Type", renderCell: (cell) => {
                    return (
                        <Chip
                            color={messageTypeChips[cell.value as keyof typeof messageTypeChips] as ChipProps['color']}
                            label={cell.value}
                        />
                    );
                }
            },
            {...dataGridTemplate, field: "protocolVersion", headerName: "Protocol version"},
            {...dataGridTemplate, field: "originatingCountry", headerName: "Originating Country"},
            {...dataGridTemplate, field: "createdTimestamp", headerName: "Created"}
        ];
    } else if (field === "subscriptions") {
        nestedData = row.subscriptions.map((subscription: any) => ({
            id: subscription.id,
            status: subscription.status,
            selector: subscription.selector,
            errorMessage: subscription.errorMessage,
            consumerCommonName: subscription.consumerCommonName,
            connections: subscription.connections,
            description: subscription.description,
            endpoints: subscription.endpoints,
            lastUpdated: timeConverter(subscription.lastUpdated)
        }));
        nestedConnectionData = row.subscriptions.flatMap((subscription: any) => {
            return subscription.connections.map((connection: any) => ({
                subscriptionId: `${subscription.id}`,
                id: connection.id,
                source: connection.source,
                destination: connection.destination
            }));
        });

        nestedConnectionColumns = [
            {...dataGridTemplate, field: "id", headerName: "ID"},
            {...dataGridTemplate, field: "source", headerName: "Source"},
            {...dataGridTemplate, field: "destination", headerName: "Destination"},
        ];

        nestedColumns = [
            {...dataGridTemplate, field: "id", headerName: "ID"},
            {...dataGridTemplate, field: "consumerCommonName", headerName: "Consumer common name"},
            {
                ...dataGridTemplate, field: "status", headerName: "Status", renderCell: (cell) => {
                    return (
                        <Chip
                            color={statusChips[cell.value as keyof typeof statusChips] as ChipProps['color']}
                            label={cell.value}
                        />
                    );
                }
            },

            {
                ...dataGridTemplate, field: "connections", headerName: "Connections",
                renderCell: (params) => {
                    const connections = params.row.connections;
                    return (
                        <Box
                            style={{cursor: "pointer"}}
                            onClick={() => {
                                const rowId = params.row.id;
                                handleCellClick(params.row.connections, "connections", rowId)
                            }}
                        >
                            {Array.isArray(connections) ?
                                <StyledBorderlineSpan> {connections.length} </StyledBorderlineSpan> :
                                <StyledBorderlineSpan> : 0 </StyledBorderlineSpan>}
                        </Box>
                    );
                },
            },
            {...dataGridTemplate, field: "description", headerName: "Description"},
            {...dataGridTemplate, field: "lastUpdated", headerName: "Last Updated"},
        ];
    } else if (field === "deliveries") {
        nestedData = row.deliveries.map((delivery: any) => ({
            id: delivery.id,
            status: delivery.status,
            selector: delivery.selector,
            description: delivery.description,
            endpoints: delivery.endpoints,
            lastUpdatedTimestamp: timeConverter(delivery.lastUpdatedTimestamp)
        }));

        nestedColumns = [
            {...dataGridTemplate, field: "id", headerName: "ID"},
            {
                ...dataGridTemplate, field: "status", headerName: "Status", renderCell: (cell) => {
                    return (
                        <Chip
                            color={statusChips[cell.value as keyof typeof statusChips] as ChipProps['color']}
                            label={cell.value}
                        />
                    );
                }
            },
            {...dataGridTemplate, field: "description", headerName: "Description"},
            {...dataGridTemplate, field: "lastUpdatedTimestamp", headerName: "Last Updated"}
        ];
    } else if (field === "privateChannels" && row.privatechannels) {
        nestedData = row.privatechannels.map((privateChannel: any) => ({
            id: privateChannel.id,
            status: privateChannel.status,
            peers: privateChannel.peers,
            description: privateChannel.description,
            endpoint: privateChannel.endpoint,
            lastUpdated: timeConverter(privateChannel.lastUpdated)
        }));

        nestedColumns = [
            {
                ...dataGridTemplate, field: "id", headerName: "ID", renderCell: (params) => {
                    const value = params.row.id;
                    return value ? value.substring(0, 8) : '';
                }
            },
            {
                ...dataGridTemplate, field: "status", headerName: "Status", renderCell: (cell) => {
                    return (
                        <Chip
                            color={statusChips[cell.value as keyof typeof statusChips] as ChipProps['color']}
                            label={cell.value}
                        />
                    );
                }
            },
            {
                ...dataGridTemplate, field: "peers", headerName: "Number of peers", renderCell: (params) => {
                    const value = params.row.peers;
                    return Array.isArray(value) ? value.length : 0;
                },
            },
            {...dataGridTemplate, field: "description", headerName: "Description"},
            {...dataGridTemplate, field: "lastUpdated", headerName: "Last Updated"}
        ];
    }

    const getHeader = () => {
        return field ? field.charAt(0).toUpperCase() + field.slice(1) : '';
    }
    const headerContent = getHeader();

    return (
        <Box flex={1}>
            <Mainheading>{headerContent}</Mainheading>
            <Subheading>
                These are all of {field}. You can click a row to view more information.
            </Subheading>
            <Divider sx={{marginY: 3}}/>
            <Box sx={{height: 450, width: "100%"}}>
            <Box sx={StyledTableHeader}>
                <motion.div
                    animate={{backgroundColor: isFlashing ? "#ffbf7d" : "#f0f1f1"}}
                    transition={{duration: 0.3, ease: "easeInOut"}}
                    style={{padding: "5px", borderRadius: "8px"}}
                >
                    <DataGrid
                        rows={nestedData}
                        columns={nestedColumns}
                        getRowId={(row) => row.id}
                        onRowClick={handleOnRowClick}
                        sort={{field: "createdTimestamp", sort: "desc"}}
                        slots={{
                            noRowsOverlay: CustomEmptyOverlay
                        }}
                        onCellClick={(params) => {
                            setHighlightedCell({id: params.id as number, field: params.field});
                        }}
                        getCellClassName={(params) =>
                            params.field === 'connections' &&
                            highlightedCell.id === params.id && highlightedCell.field === params.field
                                ? "highlighted-cell"
                                : ""
                        }
                    />
                </motion.div>
            </Box>
                {serviceProviderRow && field === 'capabilities' && (
                    <CapabilityDrawer
                        handleMoreClose={handleMoreClose}
                        open={drawerOpen}
                        capabilities={serviceProviderRow as ServiceProviderCapabilities}
                    />
                )}
                {(serviceProviderRow && field === 'subscriptions' && highlightedCell.field != 'connections') && (
                    <CommonDrawer
                        handleMoreClose={handleMoreClose}
                        open={drawerOpen}
                        commonAttributes={serviceProviderRow as ServiceProviderSubscriptions | ServiceProviderDeliveries}
                        heading={headerContent}
                    />
                )}
                {serviceProviderRow && field === 'deliveries' && (
                    <CommonDrawer
                        handleMoreClose={handleMoreClose}
                        open={drawerOpen}
                        commonAttributes={serviceProviderRow as ServiceProviderSubscriptions | ServiceProviderDeliveries}
                        heading={headerContent}
                    />
                )}
                {serviceProviderRow && field === 'privateChannels' && (
                    <PrivateChannelDrawer
                        handleMoreClose={handleMoreClose}
                        open={drawerOpen}
                        privateChannel={serviceProviderRow as ServiceProviderPrivateChannels}
                    />
                )}
            </Box>
            {field === 'subscriptions' ? Object.keys(expandedRows).map((rowId) => {
                const filteredConnections = nestedConnectionData.filter(
                    (connection: any) => connection.subscriptionId === serviceProviderRow?.id
                );
                if (!row || !serviceProviderRow) {
                    return null;
                }

                return (
                    <Box key={rowId} sx={{height: 100, width: "100%"}}>
                        <NestedGridConnections
                            row={serviceProviderRow}
                            nestedConnectionData={filteredConnections}
                            nestedConnectionColumns={nestedConnectionColumns}
                            isFlashing={isFlashing}
                        />
                    </Box>
                );
            }) : null}
        </Box>
    );
}
export default NestedGridServiceProviders;