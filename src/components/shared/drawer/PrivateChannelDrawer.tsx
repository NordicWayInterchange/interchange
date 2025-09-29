import {
    Box, Divider,
    Drawer, FormControl, IconButton, InputAdornment,
    List,
    ListItem, ListItemText, TextField,
    Toolbar, Typography
} from "@mui/material";
import React from "react";
import CloseIcon from "@mui/icons-material/Close";
import {drawerStyle, StyledCard, StyledHeaderBox} from "@/components/styles/StyledElements";
import {ContentCopy} from "@/components/shared/actions/ContentCopy";
import Loading from "@/components/shared/components/Loading";
import {ServiceProviderPrivatechannels, ServiceProviderPrivateChannelsPeer} from "@/types/serviceProviders";
import WarningAmberIcon from "@mui/icons-material/WarningAmber";
import {Chip} from "@/components/shared/components/Chip";
import {colorMapping, statusChips} from "@/lib/statusChips";

type Props = {
    privateChannel: ServiceProviderPrivatechannels | ServiceProviderPrivateChannelsPeer;
    open: boolean;
    title: string;
    handleMoreClose: () => void;
};

const PrivateChannelDrawer = ({privateChannel, open, title, handleMoreClose}: Props) => {
    if (!privateChannel) {
        return <Loading text=""/>
    }

    const statusKey = privateChannel.status.toString() as keyof typeof statusChips;
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
                                <Typography>{title} details</Typography>
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
                                        <ListItemText primary={"ID"} secondary={privateChannel.id}/>
                                    </Box>
                                    <Box>
                                        <ListItemText
                                            primary={"Last updated"}
                                            secondary={privateChannel.lastUpdated}
                                        />
                                    </Box>
                                </Box>
                            </StyledCard>
                        </ListItem>

                        {privateChannel.peers && (
                            <ListItem>
                                <StyledCard variant={"outlined"}>
                                    <Typography  sx={{ marginBottom: 4 }}>Peers</Typography>
                                    <Box sx={peerListStyle} />
                                    {privateChannel.peers.length > 0 ? privateChannel.peers.map((item, index) => (
                                        <React.Fragment key={index}>
                                            <ListItem>
                                                <ListItemText
                                                    primary={<Typography component="div" sx={primaryTextStyle}>{item}</Typography>}
                                                    secondary={
                                                        <Box sx={secondaryContainerStyle}>
                                                            <ContentCopy value={item} />
                                                        </Box>
                                                    }
                                                />
                                                <Box>
                                                </Box>
                                            </ListItem>
                                            {index < privateChannel.peers.length - 1 && <Divider
                                                sx={{ borderStyle: "dashed", borderWidth: 1, marginX: 2, position: "relative", marginBottom: "-8px", top: "-20px" }} />}
                                        </React.Fragment>
                                    )) : <Box sx={warningStyle}>
                                        <WarningAmberIcon sx={{ mr: "16px", mt: "-7px" }} />
                                        <Typography variant="body2" sx={{ fontWeight: "500" }}>There is no peer for this private channel</Typography>
                                    </Box>}
                                </StyledCard>
                            </ListItem>
                        )}

                        {privateChannel.endpoint && (
                            <ListItem>
                                <StyledCard variant={"outlined"}>
                                    <Typography>Endpoint</Typography>
                                    <FormControl fullWidth>
                                        <TextField
                                            value={privateChannel.endpoint.host}
                                            label={"Host"}
                                            margin="normal"
                                            slotProps={{
                                                input: {
                                                    endAdornment: (
                                                        <InputAdornment position="end">
                                                            <ContentCopy value={privateChannel.endpoint.host} />
                                                        </InputAdornment>
                                                    ),
                                                },
                                            }}
                                        />
                                        <TextField
                                            value={privateChannel.endpoint.port}
                                            label="Port"
                                            margin="normal"
                                            slotProps={{
                                                input: {
                                                    endAdornment: (
                                                        <InputAdornment position="end">
                                                            <ContentCopy value={privateChannel.endpoint.port.toString()} />
                                                        </InputAdornment>
                                                    ),
                                                },
                                            }}
                                        />
                                        <TextField
                                            value={privateChannel.endpoint.queueName}
                                            label="Queue name"
                                            margin="normal"
                                            slotProps={{
                                                input: {
                                                    endAdornment: (
                                                        <InputAdornment position="end">
                                                            <ContentCopy value={privateChannel.endpoint.queueName} />
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
                            <StyledCard variant={"outlined"}>
                                <Typography>Description</Typography>
                                <div>
                                    <FormControl fullWidth>
                                        <TextField
                                            margin="normal"
                                            multiline
                                            value={privateChannel.description || ""}
                                            rows={4}
                                            slotProps={{
                                                input: {
                                                    endAdornment: (
                                                        <InputAdornment position="end">
                                                            <ContentCopy value={privateChannel.description} />
                                                        </InputAdornment>
                                                    ),
                                                },
                                            }}
                                        />
                                    </FormControl>
                                </div>
                            </StyledCard>
                        </ListItem>
                    </List>
                </Box>
            </Drawer>
        </>
    );
};


const peerListStyle = {
    height: "1.5px",
    flexGrow: 1,
    backgroundColor: "#E67600",
    position: "relative",
    top: "-15px"
};

const warningStyle = {
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    color: "#E67600",
    padding: "4px 12px",
    borderRadius: "6px",
    boxShadow: 1,
    height: "40px",
    backgroundColor: "#FFF7E6",
    position: "relative", top: "-20px"
};

const primaryTextStyle = {
    maxWidth: "calc(100% - 32px)",
    overflow: "hidden",
    textOverflow: "ellipsis",
    whiteSpace: "nowrap",
    position: "relative",
    top: "-22px",
    marginBottom: "-25px"
};

const secondaryContainerStyle = {
    display: "flex",
    justifyContent: "flex-end",
    width: "100%",
    ml: "9px",
    position: "relative",
    top: "-16px"
};

export default PrivateChannelDrawer;
