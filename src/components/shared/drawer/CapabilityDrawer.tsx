import {
    Box,
    Card, CardProps,
    Drawer, FormControl, IconButton, InputAdornment, InputLabel,
    List,
    ListItem, ListItemText, MenuItem, Select, TextField,
    Toolbar, Typography
} from "@mui/material";
import React from "react";
import CloseIcon from "@mui/icons-material/Close";
import {styled} from "@mui/material/styles";
import {drawerStyle, StyledButton} from "@/components/styles/StyledElements";
import {ContentCopy} from "@/components/shared/actions/ContentCopy";
import {Capability} from "@/types/neighbours";

type Props = {
    capabilities: Capability;
    open: boolean;
    handleMoreClose: () => void;
};

const CapabilityDrawer = ({capabilities, open, handleMoreClose}: Props) => {

    const application = capabilities.application;

    return (
        <>
        <Drawer
            sx={drawerStyle}
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
                        <StyledCard variant="outlined">
                            <Box sx={{display: "flex", justifyContent: "space-between"}}>
                                <Box>
                                    <ListItemText primary={"ID"} secondary={capabilities.id}/>
                                </Box>
                                <Box>
                                    <ListItemText
                                        primary={"Last updated"}
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
                                                    <ContentCopy value={application.publisherId} />
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
                                                    <ContentCopy value={application.publicationId} />
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
                                                        <ContentCopy value={application.publicationType} />
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
                                                        <ContentCopy value={application.publisherName} />
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
                                                    <ContentCopy value={application.originatingCountry} />
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
                                                    <ContentCopy value={application.messageType} />
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
                                                    <ContentCopy value={application.protocolVersion} />
                                                </InputAdornment>
                                            ),
                                        },
                                    }}
                                />
                                {application.causeCodesDictionary && (
                                    <FormControl margin="normal">
                                        <InputLabel>Cause codes</InputLabel>
                                        <Select
                                            MenuProps={{ PaperProps: { sx: { maxHeight: 200 } } }}
                                            label="Cause codes"
                                            multiple
                                            defaultValue={application.causeCodesDictionary.map(
                                                (cause) => {
                                                    return cause["value"];
                                                }
                                            )}
                                        >
                                            {application.causeCodesDictionary.map((cause, index) => {
                                                return (
                                                    <StyledMenuItem
                                                        disabled
                                                        key={index}
                                                        value={cause.value}
                                                    >
                                                        {cause.value}: {cause.label}
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
                                    sx={{mt:2.75}}
                                    color="buttonThemeColor"
                                    variant="outlined"
                                >
                                    Show map
                                </StyledButton>
                            </FormControl>
                        </StyledCard>
                    </ListItem>
            </List>
        </Box>
        </Drawer>
</>
);
};


const StyledCard = styled(Card)<CardProps>(() => ({
    padding: '16px',
    width: '100%',
}));

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
