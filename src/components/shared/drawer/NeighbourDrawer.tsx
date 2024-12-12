import {
    Box,
    Card,
    Drawer, FormControl, IconButton, InputAdornment,
    List,
    ListItem, ListItemText, TextField,
    Toolbar, Typography
} from "@mui/material";
import React, { useState } from "react";
import { useSession } from "next-auth/react";
import CloseIcon from "@mui/icons-material/Close";
import { timeConverter } from "@/lib/timeConverter";
import { styled } from "@mui/material/styles";
import {drawerStyle} from "@/components/styles/StyledElements";
import {ContentCopy} from "@/components/shared/actions/ContentCopy";
import neighbours from "@/pages/neighbours";

type Props = {
    item: neighbours;
    open: boolean;
    handleMoreClose: () => void;
};

const NeighbourDrawer = ({item, open, handleMoreClose }: Props) => {
    const [dialogOpen, setDialogOpen] = useState<boolean>(false);
    const { data: session } = useSession();

    const handleClickClose = (close: boolean) => {
        setDialogOpen(close);
    };

    const getAttribute = () => {
       // return label == "Delivery" ? item.endpoints[0].target: item.endpoints[0].source;
    };

    return (
        <>
            <Drawer
                sx={drawerStyle}
                variant="temporary"
                anchor="right"
                open={open}
                onClose={handleMoreClose}
            >
                <Toolbar />
                <Box sx={{ padding: 1 }}>
                    <List>
                        <ListItem sx={{ justifyContent: "flex-end" }}>
                            <IconButton onClick={handleMoreClose}>
                                <CloseIcon />
                            </IconButton>
                        </ListItem>>
                        <ListItem>
                            <StyledCard variant={"outlined"}>
                                <Box sx={{ display: "flex", justifyContent: "space-between" }}>
                                    <Box>
                                        <ListItemText primary={"ID"} secondary={item.id} />
                                    </Box>
                                    <Box>
                                        <ListItemText
                                            primary={"Last updated"}
                                            secondary={timeConverter(item.createdTimestamp)}
                                        />
                                    </Box>
                                </Box>
                            </StyledCard>
                        </ListItem>
                        {item.endpoints?.length > 0 && (
                            <ListItem>
                                <StyledCard variant={"outlined"}>
                                    <Typography>Endpoints</Typography>
                                    <FormControl fullWidth>
                                        <TextField
                                            value={item.endpoints[0].host || ""}
                                            label="Host"
                                            margin="normal"
                                            slotProps={{
                                                input: {
                                                    endAdornment: (
                                                        <InputAdornment position="end">
                                                            <ContentCopy value={item.endpoints[0].host} />
                                                        </InputAdornment>
                                                    ),
                                                },
                                            }}
                                        />
                                        <TextField
                                            value={getAttribute() || ""}
                                            label="Source"
                                            margin="normal"
                                            slotProps={{
                                                input: {
                                                    endAdornment: (
                                                        <InputAdornment position="end">
                                                            <ContentCopy value={item.endpoints[0].source} />
                                                        </InputAdornment>
                                                    ),
                                                },
                                            }}
                                        />
                                        <TextField
                                            value={item.endpoints[0].port || ""}
                                            label="Port"
                                            margin="normal"
                                            slotProps={{
                                                input: {
                                                    endAdornment: (
                                                        <InputAdornment position="end">
                                                            <ContentCopy value={item.endpoints[0].port.toString()} />,
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
                                <Typography>Selector</Typography>
                                <FormControl fullWidth>
                                    <TextField
                                        margin="normal"
                                        multiline
                                        value={item.selector || ""}
                                        rows={4}
                                        slotProps={{
                                            input: {
                                                endAdornment: (
                                                    <InputAdornment position="end">
                                                        <ContentCopy value={item.selector} />
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


const StyledCard = styled(Card)(({}) => ({
    padding: "16px",
    width: "100%",
}));

const StyledHeaderBox = styled(Box)(({}) => ({
    display: "flex",
    alignItems: "center",
    justifyContent: "space-between",
    width: "100%",
}));


export default NeighbourDrawer;
