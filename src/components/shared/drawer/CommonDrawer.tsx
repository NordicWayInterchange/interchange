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
import {Chip} from "@/components/shared/components/Chip";
import {colorMapping, statusChips} from "@/lib/statusChips";
import {ServiceProviderDeliveries, ServiceProviderSubscriptions} from "@/types/serviceProviders";
import QueueValidator from "@/components/shared/actions/QueueValidator";
import ExchangeValidator from "@/components/shared/actions/ExchangeValidator";
import Loading from "@/components/shared/components/Loading";
import {Delivery} from "@/types/GraphSection";
import {timeConverter} from "@/lib/timeConverter";

type Props = {
    commonAttributes: Subscription | ServiceProviderSubscriptions | ServiceProviderDeliveries | Delivery;
    open: boolean;
    handleMoreClose: () => void;
    heading: string;
};

const CommonDrawer = ({commonAttributes, open, handleMoreClose, heading}: Props) => {
    if (!commonAttributes) {
        return  <Loading text=""/>;
    }
    const subscriptionStatus = (commonAttributes as any)?.subscriptionStatus;
    const consumerCommonName = (commonAttributes as any)?.consumerCommonName;
    const path = (commonAttributes as any)?.path;
    const errorMessage = (commonAttributes as any)?.errorMessage;
    const statusKey = (subscriptionStatus?.toString() || commonAttributes.status?.toString()) as keyof typeof statusChips;
    const chipColor = colorMapping[statusChips[statusKey]] || "default";
    return (
        <>
            <Drawer
                sx={drawerStyle}
                slotProps={{paper: {sx: {backgroundColor: "#F9F9F9"}}}}
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
                                        <ListItemText primary={"ID"} secondary={commonAttributes.id}/>
                                    </Box>
                                    <Box>
                                        <ListItemText
                                            primary={"Last updated"}
                                            secondary={commonAttributes.lastUpdatedTimestamp ? timeConverter(commonAttributes.lastUpdatedTimestamp) : (commonAttributes as any)?.lastUpdated}
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
                        {commonAttributes.endpoints?.length > 0 && (
                            <ListItem>
                                <StyledCard variant="outlined">
                                    <Typography>Endpoints</Typography>
                                    <FormControl fullWidth>
                                        {commonAttributes.endpoints[0].source &&
                                            <QueueValidator queueName={commonAttributes.endpoints[0].source}/>}
                                        {commonAttributes.endpoints[0].target &&
                                            <ExchangeValidator exchangeName={commonAttributes.endpoints[0].target}/>}
                                        <TextField
                                            value={commonAttributes.endpoints[0].host || ""}
                                            label="Host"
                                            margin="normal"
                                            slotProps={{
                                                input: {
                                                    endAdornment: (
                                                        <InputAdornment position="end">
                                                            <ContentCopy value={commonAttributes.endpoints[0].host}/>
                                                        </InputAdornment>
                                                    ),
                                                },
                                            }}
                                        />
                                        {commonAttributes.endpoints[0].source && (
                                            <TextField
                                                value={commonAttributes.endpoints[0].source || ""}
                                                label="Source"
                                                margin="normal"
                                                slotProps={{
                                                    input: {
                                                        endAdornment: (
                                                            <InputAdornment position="end">
                                                                <ContentCopy
                                                                    value={commonAttributes.endpoints[0].source}/>
                                                            </InputAdornment>
                                                        ),
                                                    },
                                                }}
                                            />
                                        )}
                                        {commonAttributes.endpoints[0].target && (
                                            <TextField
                                                value={commonAttributes.endpoints[0].target || ""}
                                                label="Target"
                                                margin="normal"
                                                slotProps={{
                                                    input: {
                                                        endAdornment: (
                                                            <InputAdornment position="end">
                                                                <ContentCopy
                                                                    value={commonAttributes.endpoints[0].target}/>
                                                            </InputAdornment>
                                                        ),
                                                    },
                                                }}
                                            />
                                        )}
                                        <TextField
                                            value={commonAttributes.endpoints[0].port || ""}
                                            label="Port"
                                            margin="normal"
                                            slotProps={{
                                                input: {
                                                    endAdornment: (
                                                        <InputAdornment position="end">
                                                            <ContentCopy
                                                                value={commonAttributes.endpoints[0].port.toString() || ''}/>
                                                        </InputAdornment>
                                                    ),
                                                },
                                            }}
                                        />
                                        {commonAttributes.endpoints[0].dlqName && (
                                            <TextField
                                                value={commonAttributes.endpoints[0].dlqName || ""}
                                                label="Dead letter queue"
                                                margin="normal"
                                                slotProps={{
                                                    input: {
                                                        endAdornment: (
                                                            <InputAdornment position="end">
                                                                <ContentCopy
                                                                    value={commonAttributes.endpoints[0].dlqName}/>
                                                            </InputAdornment>
                                                        ),
                                                    },
                                                }}
                                            />
                                        )}
                                        {commonAttributes.endpoints[0].maxBandwidth && (<TextField
                                            value={commonAttributes.endpoints[0].maxBandwidth || ""}
                                            label="Max bandwidth"
                                            margin="normal"
                                            slotProps={{
                                                input: {
                                                    endAdornment: (
                                                        <InputAdornment position="end">
                                                            <ContentCopy
                                                                value={commonAttributes.endpoints[0].maxBandwidth.toString() || ''}/>,
                                                        </InputAdornment>
                                                    ),
                                                },
                                            }}
                                        />)}
                                        {commonAttributes.endpoints[0].maxMessageRate && (<TextField
                                            value={commonAttributes.endpoints[0].maxMessageRate || ""}
                                            label="Max message rate"
                                            margin="normal"
                                            slotProps={{
                                                input: {
                                                    endAdornment: (
                                                        <InputAdornment position="end">
                                                            <ContentCopy
                                                                value={commonAttributes.endpoints[0].maxMessageRate.toString() || ''}/>,
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
                                        value={commonAttributes.selector || ""}
                                        rows={4}
                                        slotProps={{
                                            input: {
                                                endAdornment: (
                                                    <InputAdornment position="end">
                                                        <ContentCopy value={commonAttributes.selector}/>
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
