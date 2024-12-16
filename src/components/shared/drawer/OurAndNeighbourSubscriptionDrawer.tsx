import {
    Box,
    Card, CardProps,
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

type Props = {
    subscriptions: Subscription;
    open: boolean;
    handleMoreClose: () => void;
    heading: string;
};

const OurAndNeighbourSubscriptionDrawer = ({subscriptions, open, handleMoreClose, heading}: Props) => {
    if (!subscriptions) {
        return <Typography>Loading...</Typography>;
    }
    return (
        <>
            <Drawer
                sx={drawerStyle}
                PaperProps={{ sx: {backgroundColor: "#F9F9F9"}}}
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
                                    color={
                                        statusChips[
                                            subscriptions.subscriptionStatus.toString() as keyof typeof statusChips
                                            ] as any
                                    }
                                    label={subscriptions.subscriptionStatus}
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
                                            secondary={subscriptions.lastUpdatedTimestamp}
                                        />
                                    </Box>
                                </Box>
                                <FormControl fullWidth>
                                    <TextField
                                        value={subscriptions.path || ""}
                                        label="Path"
                                        margin="normal"
                                        slotProps={{
                                            input: {
                                                endAdornment: (
                                                    <InputAdornment position="end">
                                                        <ContentCopy value={subscriptions.path}/>
                                                    </InputAdornment>
                                                ),
                                            },
                                        }}
                                    />
                                    <TextField
                                        value={subscriptions.consumerCommonName || ""}
                                        label="Consumer common name"
                                        margin="normal"
                                        slotProps={{
                                            input: {
                                                endAdornment: (
                                                    <InputAdornment position="end">
                                                        <ContentCopy value={subscriptions.consumerCommonName}/>
                                                    </InputAdornment>
                                                ),
                                            },
                                        }}
                                    />
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
                                        <TextField
                                            value={subscriptions.endpoints[0].port || ""}
                                            label="Port"
                                            margin="normal"
                                            slotProps={{
                                                input: {
                                                    endAdornment: (
                                                        <InputAdornment position="end">
                                                            <ContentCopy
                                                                value={subscriptions.endpoints[0].port.toString() || ''}/>,
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

export default OurAndNeighbourSubscriptionDrawer;
