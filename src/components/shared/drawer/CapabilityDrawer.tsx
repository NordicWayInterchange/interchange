import {
    Box,
    Drawer, FormControl, IconButton, InputAdornment, InputLabel,
    List,
    ListItem, ListItemText, MenuItem, Select, TextField,
    Toolbar, Typography
} from "@mui/material";
import React, {useState} from "react";
import CloseIcon from "@mui/icons-material/Close";
import {styled} from "@mui/material/styles";
import {drawerStyle, StyledButton, StyledCard, StyledHeaderBox} from "@/components/styles/StyledElements";
import {ContentCopy} from "@/components/shared/actions/ContentCopy";
import {Capability} from "@/types/neighbours";
import MapDialog from "@/components/map/MapDialog";
import {extractMatchingCauseCodes} from "@/lib/extractMatchingCauseCodes";
import Loading from "@/components/shared/components/Loading";

type Props = {
    capabilities: Capability;
    open: boolean;
    handleMoreClose: () => void;
};

const CapabilityDrawer = ({capabilities, open, handleMoreClose}: Props) => {
    const [openMap, setOpenMap] = useState<boolean>(false);
    const application = capabilities.application;
    const metaData = capabilities.metadata;
    if (!capabilities || !application || !metaData) {
        return <Loading text=""/>
    }

    const handleClose = () => {
        setOpenMap(false);
    };

    const causeCode = extractMatchingCauseCodes(application?.causeCode);

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
                                <Typography>Capabilities details</Typography>
                            </StyledHeaderBox>
                        </ListItem>
                        <ListItem>
                            <StyledCard variant="outlined">
                                <Box sx={{display: "flex", justifyContent: "space-between"}}>
                                    <Box>
                                        <ListItemText primary={"ID"} secondary={capabilities.id}/>
                                    </Box>
                                    <Box>
                                        <ListItemText
                                            primary={"Created"}
                                            secondary={capabilities.createdTimestamp}
                                        />
                                    </Box>
                                </Box>
                            </StyledCard>
                        </ListItem>
                        <ListItem>
                            <StyledCard variant="outlined">
                                <Typography>Publisher</Typography>
                                <FormControl fullWidth>
                                    <TextField
                                        value={application.publisherId}
                                        label="Publisher ID"
                                        margin="normal"
                                        slotProps={{
                                            input: {
                                                endAdornment: (
                                                    <InputAdornment position="end">
                                                        <ContentCopy value={application.publisherId}/>
                                                    </InputAdornment>
                                                ),
                                            },
                                        }}
                                    />
                                    <TextField
                                        value={application.publicationId}
                                        label="Publication ID"
                                        margin="normal"
                                        slotProps={{
                                            input: {
                                                endAdornment: (
                                                    <InputAdornment position="end">
                                                        <ContentCopy value={application.publicationId}/>
                                                    </InputAdornment>
                                                ),
                                            },
                                        }}
                                    />
                                    {application.publicationType && (
                                        <TextField
                                            value={application.publicationType}
                                            label="Publication type"
                                            margin="normal"
                                            slotProps={{
                                                input: {
                                                    endAdornment: (
                                                        <InputAdornment position="end">
                                                            <ContentCopy value={application.publicationType}/>
                                                        </InputAdornment>
                                                    ),
                                                },
                                            }}
                                        />
                                    )}
                                    {application.publisherName && (
                                        <TextField
                                            value={application.publisherName}
                                            label="Publisher name"
                                            margin="normal"
                                            slotProps={{
                                                input: {
                                                    endAdornment: (
                                                        <InputAdornment position="end">
                                                            <ContentCopy value={application.publisherName}/>
                                                        </InputAdornment>
                                                    ),
                                                },
                                            }}
                                        />
                                    )}
                                    <TextField
                                        value={application.originatingCountry}
                                        label="Originating Country"
                                        margin="normal"
                                        slotProps={{
                                            input: {
                                                endAdornment: (
                                                    <InputAdornment position="end">
                                                        <ContentCopy value={application.originatingCountry}/>
                                                    </InputAdornment>
                                                ),
                                            },
                                        }}
                                    />
                                </FormControl>
                            </StyledCard>
                        </ListItem>

                        <ListItem>
                            <StyledCard variant="outlined">
                                <Typography>Message</Typography>
                                <FormControl fullWidth>
                                    <TextField
                                        value={application.messageType}
                                        label="Message Type"
                                        margin="normal"
                                        slotProps={{
                                            input: {
                                                endAdornment: (
                                                    <InputAdornment position="end">
                                                        <ContentCopy value={application.messageType}/>
                                                    </InputAdornment>
                                                ),
                                            },
                                        }}
                                    />
                                    <TextField
                                        value={application.protocolVersion}
                                        label="Protocol Version"
                                        margin="normal"
                                        slotProps={{
                                            input: {
                                                endAdornment: (
                                                    <InputAdornment position="end">
                                                        <ContentCopy value={application.protocolVersion}/>
                                                    </InputAdornment>
                                                ),
                                            },
                                        }}
                                    />
                                    {causeCode && causeCode.length > 0 && (
                                        <FormControl margin="normal">
                                            <InputLabel>Cause codes</InputLabel>
                                            <Select
                                                MenuProps={{PaperProps: {sx: {maxHeight: 200}}}}
                                                label="Cause codes"
                                                multiple
                                                defaultValue={causeCode?.map(
                                                    (cause) => {
                                                        return cause["value"];
                                                    }
                                                )}
                                            >
                                                {causeCode?.map((cause, index) => {
                                                    return (
                                                        <StyledMenuItem
                                                            disabled
                                                            key={index}
                                                            value={cause.value}
                                                        >
                                                            {cause.value}{cause.label ? ':' : ''} {cause.label}
                                                        </StyledMenuItem>
                                                    );
                                                })}

                                            </Select>
                                        </FormControl>
                                    )}
                                </FormControl>
                            </StyledCard>
                        </ListItem>
                        <ListItem>
                            <StyledCard variant="outlined">
                                <Typography>Quadtree</Typography>
                                <FormControl
                                    fullWidth
                                    sx={{
                                        display: "flex",
                                        flexDirection: "row",
                                        alignItems: "center"
                                    }}
                                >
                                    <TextField
                                        value={application.quadTree}
                                        label="Hash"
                                        margin="normal"
                                        sx={{
                                            flexGrow: 1,
                                            marginRight: 1
                                        }}
                                    />
                                    <StyledButton
                                        sx={{mt: 2.75}}

                                        variant="outlined"
                                        onClick={() => setOpenMap(true)}
                                    >
                                        Show map
                                    </StyledButton>
                                </FormControl>
                            </StyledCard>
                        </ListItem>
                        <ListItem>
                            <StyledCard variant="outlined">
                                <Typography>Meta data</Typography>
                                <FormControl fullWidth>
                                    <TextField
                                        value={metaData.shardCount}
                                        label="Shard count"
                                        margin="normal"
                                        slotProps={{
                                            input: {
                                                endAdornment: (
                                                    <InputAdornment position="end">
                                                        <ContentCopy value={metaData.shardCount.toString()}/>
                                                    </InputAdornment>
                                                ),
                                            },
                                        }}
                                    />
                                    {metaData.infoUrl && (
                                        <TextField
                                            value={metaData.infoUrl}
                                            label="Info url"
                                            margin="normal"
                                            slotProps={{
                                                input: {
                                                    endAdornment: (
                                                        <InputAdornment position="end">
                                                            <ContentCopy value={metaData.infoUrl}/>
                                                        </InputAdornment>
                                                    ),
                                                },
                                            }}
                                        />
                                    )}
                                    {metaData.redirectPolicy && (
                                        <TextField
                                            value={metaData.redirectPolicy}
                                            label="Redirect Policy"
                                            margin="normal"
                                            slotProps={{
                                                input: {
                                                    endAdornment: (
                                                        <InputAdornment position="end">
                                                            <ContentCopy value={metaData.redirectPolicy}/>
                                                        </InputAdornment>
                                                    ),
                                                },
                                            }}
                                        />
                                    )}
                                    {metaData.maxBandwidth && (
                                        <TextField
                                            value={metaData.maxBandwidth}
                                            label="Max Bandwidth"
                                            margin="normal"
                                            slotProps={{
                                                input: {
                                                    endAdornment: (
                                                        <InputAdornment position="end">
                                                            <ContentCopy value={metaData.maxBandwidth.toString()}/>
                                                        </InputAdornment>
                                                    ),
                                                },
                                            }}
                                        />
                                    )}
                                    {metaData.maxMessageRate && (
                                        <TextField
                                            value={metaData.maxMessageRate}
                                            label="Max MessageRate"
                                            margin="normal"
                                            slotProps={{
                                                input: {
                                                    endAdornment: (
                                                        <InputAdornment position="end">
                                                            <ContentCopy value={metaData.maxMessageRate.toString()}/>
                                                        </InputAdornment>
                                                    ),
                                                },
                                            }}
                                        />
                                    )}
                                    {metaData.repetitionInterval && (
                                        <TextField
                                            value={metaData.repetitionInterval}
                                            label="Repetition Interval"
                                            margin="normal"
                                            slotProps={{
                                                input: {
                                                    endAdornment: (
                                                        <InputAdornment position="end">
                                                            <ContentCopy value={metaData.repetitionInterval.toString()}/>
                                                        </InputAdornment>
                                                    ),
                                                },
                                            }}
                                        />
                                    )}
                                </FormControl>
                            </StyledCard>
                        </ListItem>
                    </List>
                </Box>
            </Drawer>
            <MapDialog
                open={openMap}
                onClose={handleClose}
                quadtree={application.quadTree}
                interactive={false}
            />
        </>
    );
};


const StyledMenuItem = styled(MenuItem)(({}) => ({
    "&.MuiMenuItem-root": {
        color: "black",
        opacity: 1
    },
    "&.Mui-disabled": {
        color: "black",
        opacity: 1
    },
    "&.Mui-selected": {
        backgroundColor: "white",
        "&.Mui-focusVisible": {
            background: "white"
        }
    }
}));

export default CapabilityDrawer;
