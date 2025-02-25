import {
    Box,
    Drawer, FormControl, IconButton, InputAdornment,
    List,
    ListItem, ListItemText, TextField,
    Toolbar, Typography
} from "@mui/material";
import React from "react";
import CloseIcon from "@mui/icons-material/Close";
import {drawerStyle, StyledCard, StyledHeaderBox} from "@/components/styles/StyledElements";
import {ContentCopy} from "@/components/shared/actions/ContentCopy";
import {Subscription} from "@/types/neighbours";
import {Chip} from "@/components/shared/Chip";
import {statusChips} from "@/lib/statusChips";
import {ServiceProviderDeliveries, ServiceProviderSubscriptions} from "@/types/serviceProviders";

type Props = {
    subscriptions: Subscription | ServiceProviderSubscriptions | ServiceProviderDeliveries;
    open: boolean;
    handleMoreClose: () => void;
    session: any;
    heading: string;
};

const colorMapping: Record<string, "default" | "primary" | "secondary" | "error" | "info" | "success" | "warning"> = {
    greenDark: "success",
    depricatedLight: "error",
    yellowLight: "warning",
    blueLight: "info",
    pinkLight: "error",
    grayLight: "default",
};

const CommonDrawer = ({subscriptions, open, handleMoreClose, session, heading}: Props) => {
    if (!subscriptions) {
        return <Typography>Loading...</Typography>;
    }
    const subscriptionStatus = (subscriptions as any)?.subscriptionStatus;
    const consumerCommonName = (subscriptions as any)?.consumerCommonName;
    const path = (subscriptions as any)?.path;
    const errorMessage = (subscriptions as any)?.errorMessage;
    const statusKey = (subscriptionStatus?.toString() || subscriptions.status.toString()) as keyof typeof statusChips;
    const chipColor = colorMapping[statusChips[statusKey]] || "default";
    return (
        <>
            <Drawer
                sx={drawerStyle}
                PaperProps={{sx: {backgroundColor: "#F9F9F9"}}}
                variant="temporary"
                anchor="right"
                open={open}
                onClose={handleMoreClose}
            >
                <Toolbar/>
                <Box sx={{padding: 1}}>
                    <List>
                        <ListItem sx={{justifyContent: "flex-end"}}>
                            <IconButton onClick={handleMoreClose}>
                                <CloseIcon/>
                            </IconButton>
                        </ListItem>
                        <ListItem>
                            <StyledHeaderBox>
                                <Typography> {heading} details</Typography>
                                <Chip
                                    color={chipColor}
                                    label={statusKey}
                                />
                            </StyledHeaderBox>
                        </ListItem>
                        <ListItem>
                            <StyledCard variant="outlined">
                                <Box sx={{display: "flex", justifyContent: "space-between"}}>
                                    <Box>
                                        <ListItemText primary={"ID"} secondary={subscriptions.id}/>
                                    </Box>
                                    <Box>
                                        <ListItemText
                                            primary={"Last updated"}
                                            secondary={subscriptions.lastUpdatedTimestamp ? subscriptions.lastUpdatedTimestamp : (subscriptions as any)?.lastUpdated}
                                        />
                                    </Box>
                                </Box>
                                <FormControl fullWidth>
                                    {path && (
                                        <TextField
                                            value={path || ""}
                                            label="Path"
                                            margin="normal"
                                            slotProps={{
                                                input: {
                                                    endAdornment: (
                                                        <InputAdornment position="end">
                                                            <ContentCopy value={path || ""}/>
                                                        </InputAdornment>
                                                    ),
                                                },
                                            }}
                                        />
                                    )}
                                    {consumerCommonName && (
                                        <TextField
                                            value={consumerCommonName || ""}
                                            label="Consumer common name"
                                            margin="normal"
                                            slotProps={{
                                                input: {
                                                    endAdornment: (
                                                        <InputAdornment position="end">
                                                            <ContentCopy value={consumerCommonName}/>
                                                        </InputAdornment>
                                                    ),
                                                },
                                            }}
                                        />
                                    )}
                                </FormControl>
                            </StyledCard>
                        </ListItem>
                        {subscriptions.endpoints.length > 0 && (
                            <ListItem>
                                <StyledCard variant="outlined">
                                    <Typography>Endpoints</Typography>
                                    <FormControl fullWidth>

                                        <TextField
                                            value={subscriptions.endpoints[0].host || ""}
                                            label="Host"
                                            margin="normal"
                                            slotProps={{
                                                input: {
                                                    endAdornment: (
                                                        <InputAdornment position="end">
                                                            <ContentCopy value={subscriptions.endpoints[0].host}/>
                                                        </InputAdornment>
                                                    ),
                                                },
                                            }}
                                        />
                                        {subscriptions.endpoints[0].source && (
                                            <TextField
                                                value={subscriptions.endpoints[0].source || ""}
                                                label="Source"
                                                margin="normal"
                                                slotProps={{
                                                    input: {
                                                        endAdornment: (
                                                            <InputAdornment position="end">
                                                                <ContentCopy value={subscriptions.endpoints[0].source}/>
                                                            </InputAdornment>
                                                        ),
                                                    },
                                                }}
                                            />
                                        )}
                                        <TextField
                                            value={subscriptions.endpoints[0].port || ""}
                                            label="Port"
                                            margin="normal"
                                            slotProps={{
                                                input: {
                                                    endAdornment: (
                                                        <InputAdornment position="end">
                                                            <ContentCopy
                                                                value={subscriptions.endpoints[0].port.toString() || ''}/>
                                                        </InputAdornment>
                                                    ),
                                                },
                                            }}
                                        />
                                        {subscriptions.endpoints[0].maxBandwidth && (<TextField
                                            value={subscriptions.endpoints[0].maxBandwidth || ""}
                                            label="Max bandwidth"
                                            margin="normal"
                                            slotProps={{
                                                input: {
                                                    endAdornment: (
                                                        <InputAdornment position="end">
                                                            <ContentCopy
                                                                value={subscriptions.endpoints[0].maxBandwidth.toString() || ''}/>,
                                                        </InputAdornment>
                                                    ),
                                                },
                                            }}
                                        />)}
                                        {subscriptions.endpoints[0].maxMessageRate && (<TextField
                                            value={subscriptions.endpoints[0].maxMessageRate || ""}
                                            label="Max message rate"
                                            margin="normal"
                                            slotProps={{
                                                input: {
                                                    endAdornment: (
                                                        <InputAdornment position="end">
                                                            <ContentCopy
                                                                value={subscriptions.endpoints[0].maxMessageRate.toString() || ''}/>,
                                                        </InputAdornment>
                                                    ),
                                                },
                                            }}
                                        />)}
                                    </FormControl>
                                </StyledCard>
                            </ListItem>
                        )}
                        {errorMessage && (
                            <ListItem>
                                <StyledCard variant="outlined">
                                    <Typography>Error message</Typography>
                                    <FormControl fullWidth>
                                        <TextField
                                            value={errorMessage || ""}
                                            label="Error message"
                                            margin="normal"
                                            slotProps={{
                                                input: {
                                                    endAdornment: (
                                                        <InputAdornment position="end">
                                                            <ContentCopy value={errorMessage}/>
                                                        </InputAdornment>
                                                    ),
                                                },
                                            }}
                                        />
                                    </FormControl>
                                </StyledCard>
                            </ListItem>
                        )}
                        <ListItem>
                            <StyledCard variant="outlined">
                                <Typography>Selector</Typography>
                                <FormControl fullWidth>
                                    <TextField
                                        margin="normal"
                                        multiline
                                        value={subscriptions.selector || ""}
                                        rows={4}
                                        slotProps={{
                                            input: {
                                                endAdornment: (
                                                    <InputAdornment position="end">
                                                        <ContentCopy value={subscriptions.selector}/>
                                                    </InputAdornment>
                                                ),
                                            },
                                        }}
                                    />
                                </FormControl>
                            </StyledCard>
                        </ListItem>
                    </List>
                </Box>
            </Drawer>
        </>
    );
};

export default CommonDrawer;
