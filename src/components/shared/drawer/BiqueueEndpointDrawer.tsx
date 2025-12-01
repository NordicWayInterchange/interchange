import {
    Box, Card,
    Drawer,
    FormControl, IconButton,
    InputAdornment,
    List,
    ListItem,
    TextField,
    Toolbar,
    Typography
} from "@mui/material";
import React, { useEffect } from "react";
import { ContentCopy } from "@/components/shared/actions/ContentCopy";
import CloseIcon from "@mui/icons-material/Close";
import {drawerStyle, StyledHeaderBox} from "@/components/styles/StyledElements";
import {useFetchBiQueueEndpoint} from "@/hooks/UseFetchBiQueueEndpoint";
import {styled} from "@mui/system";

type Props = {
    open: boolean;
    onClose: () => void;
};

const BiQueueEndpointDrawer= ({ open , onClose}: Props) => {

    const { data: biQueueEndpoint, refetch} = useFetchBiQueueEndpoint();

    useEffect(() => {
        if (open) {
            refetch();
        }
    }, [open, refetch]);

    return (
        <>
            {biQueueEndpoint &&
                Object.values(biQueueEndpoint).every(
                    (v) => v !== null && v !== undefined,
                ) && (
                    <Drawer
                        sx={drawerStyle}
                        slotProps={{paper: {sx: {backgroundColor: "#F9F9F9"}}}}
                        variant="temporary"
                        anchor="right"
                        open={open}
                        onClose={onClose}
                    >
                        <Toolbar />
                        <Box sx={{ padding: 1 }}>
                            <List>
                                <ListItem sx={{ justifyContent: "flex-end" }}>
                                    <IconButton onClick={onClose}>
                                        <CloseIcon />
                                    </IconButton>
                                </ListItem>
                                <ListItem>
                                    <StyledHeaderBox>
                                        <Typography>Bi-queue details</Typography>
                                    </StyledHeaderBox>
                                </ListItem>
                                <ListItem>
                                    <StyledCard variant={"outlined"}>
                                        <Typography>Endpoint</Typography>
                                        <FormControl fullWidth>
                                            <TextField
                                                value={biQueueEndpoint.brokerExternalName}
                                                label="Host"
                                                margin="normal"
                                                slotProps={{
                                                    input: {
                                                        endAdornment: (
                                                            <InputAdornment position="end">
                                                                <ContentCopy value={biQueueEndpoint.brokerExternalName} />
                                                            </InputAdornment>
                                                        ),
                                                    },
                                                }}
                                            />
                                            <TextField
                                                value={biQueueEndpoint.messageChannelPort}
                                                label="Port"
                                                margin="normal"
                                                slotProps={{
                                                    input: {
                                                        endAdornment: (
                                                            <InputAdornment position="end">
                                                                <ContentCopy value={biQueueEndpoint.messageChannelPort?.toString()} />
                                                            </InputAdornment>
                                                        ),
                                                    },
                                                }}
                                            />
                                            <TextField
                                                value={biQueueEndpoint.queueName}
                                                label="Source"
                                                margin="normal"
                                                slotProps={{
                                                    input: {
                                                        endAdornment: (
                                                            <InputAdornment position="end">
                                                                <ContentCopy value={biQueueEndpoint.queueName} />
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
                )}
        </>
    );
};

const StyledCard = styled(Card)(({}) => ({
    padding: "16px",
    width: "100%",
}));


export default BiQueueEndpointDrawer;

