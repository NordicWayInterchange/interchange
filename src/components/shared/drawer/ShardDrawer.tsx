import {
    Box,
    Drawer, FormControl, IconButton, InputAdornment,
    List,
    ListItem, TextField,
    Toolbar, Typography
} from "@mui/material";
import React from "react";
import CloseIcon from "@mui/icons-material/Close";
import {drawerStyle, StyledCard, StyledHeaderBox} from "@/components/styles/StyledElements";
import {ContentCopy} from "@/components/shared/actions/ContentCopy";
import Loading from "@/components/shared/components/Loading";
import {Shard} from "@/types/GraphSection";

type Props = {
    shard: Shard;
    open: boolean;
    handleMoreClose: () => void;
};

const ShardDrawer = ({shard, open, handleMoreClose}: Props) => {
    if (!shard) {
        return <Loading text=""/>
    }

    console.log('shard', shard.capabilityShardApi);

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
                                <Typography>Capability shard details</Typography>
                            </StyledHeaderBox>
                        </ListItem>
                        <ListItem>
                            <StyledCard variant="outlined">
                                <FormControl fullWidth>
                                    <TextField
                                        value={shard.capabilityShardApi.shardId}
                                        label="Binding key"
                                        margin="normal"
                                        slotProps={{
                                            input: {
                                                endAdornment: (
                                                    <InputAdornment position="end">
                                                        <ContentCopy value={shard.capabilityShardApi.shardId}/>
                                                    </InputAdornment>
                                                ),
                                            },
                                        }}
                                    />
                                    <TextField
                                        value={shard.capabilityShardApi.exchangeName}
                                        label="Destination"
                                        margin="normal"
                                        slotProps={{
                                            input: {
                                                endAdornment: (
                                                    <InputAdornment position="end">
                                                        <ContentCopy value={shard.capabilityShardApi.exchangeName}/>
                                                    </InputAdornment>
                                                ),
                                            },
                                        }}
                                    />
                                </FormControl>
                                <FormControl fullWidth>
                                    <TextField
                                        margin="normal"
                                        label="Selector"
                                        multiline
                                        value={shard.capabilityShardApi.selector}
                                        rows={4}
                                        slotProps={{
                                            input: {
                                                endAdornment: (
                                                    <InputAdornment position="end">
                                                        <ContentCopy value={shard.capabilityShardApi.selector}/>
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

export default ShardDrawer;
