import {Box, Stack} from "@mui/system";
import {IconButton, Typography} from "@mui/material";
import React, { useState} from "react";
import {frontPageCardStyle} from "@/components/styles/StyledElements";
import BiQueueEndpointDrawer from "@/components/shared/drawer/BiqueueEndpointDrawer";
import CheckCircleOutlineIcon from "@mui/icons-material/CheckCircleOutline";

const BiQueue = () => {
    const [hasAccess, setHasAccess] = useState(false);
    const [open, setOpen] = React.useState(false);


    const handleOpen = (value: boolean) => () => setOpen(value);

    const handleClose = () => setOpen(false);

    return (
        <>
            <Box sx={frontPageCardStyle}>
                <Box>
                    <Stack
                        direction="row"
                        alignItems="left"
                        spacing={2}
                        sx={{
                            flexWrap: "wrap",
                            rowGap: 1,
                        }}
                    >
                        <Stack
                            direction="row"
                            alignItems="left"
                            spacing={1}
                            onClick={handleOpen(true)}
                        >
                            <IconButton size="small">
                                <CheckCircleOutlineIcon color="success" />
                            </IconButton>
                            <Typography
                                variant="body2"
                                sx={{display: "flex", alignItems: "center"}}
                            >
                               Click here to view bi-queue endpoint.
                            </Typography>
                        </Stack>

                    </Stack>
                </Box>
            </Box>
            <BiQueueEndpointDrawer open={open} onClose={handleClose}/>
        </>
    );
};

export default BiQueue;
