import { Box, Stack } from "@mui/system";
import { IconButton, Typography } from "@mui/material";
import React, { useEffect, useState } from "react";
import CheckCircleOutlineIcon from "@mui/icons-material/CheckCircleOutline";
import InfoOutlinedIcon from "@mui/icons-material/InfoOutlined";
import LockOutlinedIcon from "@mui/icons-material/LockOutlined";
import { useSession } from "next-auth/react";
import Loading from "@/components/shared/components/Loading";
import {frontPageCardStyle, StyledButton} from "@/components/styles/StyledElements";
import BiQueueEndpointDrawer from "@/components/shared/drawer/BiqueueEndpointDrawer";
import {useFetchAccessToBiQueue} from "@/hooks/useFetchAccessToBiQueue";

const BiQueue = () => {
    const { data: session } = useSession();

    const { data: biQueueAccess, isLoading } = useFetchAccessToBiQueue(
        session?.user?.commonName as string,
    );
    const [hasAccess, setHasAccess] = useState(false);
    const [open, setOpen] = React.useState(false);


    useEffect(() => {
        if (biQueueAccess?.access !== undefined) {
            setHasAccess(biQueueAccess.access ?? false);
        }
    }, [biQueueAccess]);

    const handleOpen = (value: boolean) => () => setOpen(value);

    const handleClose = () => setOpen(false);

    return (
        <>
            <Box sx={frontPageCardStyle}>
                {biQueueAccess === undefined || isLoading ? (
                    <Loading text="Bi queue access status" />
                ) : (
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
                            {hasAccess ? (
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
                                        sx={{ display: "flex", alignItems: "center" }}
                                    >
                                        I currently have permission to bi-queue. Click here to view bi-queue endpoint.
                                    </Typography>
                                </Stack>
                            ) : (
                                <Stack direction="row" alignItems="left" spacing={1} onClick={handleOpen(true)}>
                                    <IconButton size="small">
                                        <LockOutlinedIcon color="action" />
                                    </IconButton>
                                    <Typography
                                        variant="body2"
                                        sx={{ display: "flex", alignItems: "center" }}
                                    >
                                        I currently do not have permission to bi-queue. Click here to view bi-queue endpoint.
                                            <IconButton size="small">
                                                <InfoOutlinedIcon fontSize="small" sx={{ mt: -2 }} />
                                            </IconButton>
                                    </Typography>
                                </Stack>
                            )}
                        </Stack>
                    </Box>
                )}
            </Box>
            <BiQueueEndpointDrawer open={open} onClose={handleClose} />
        </>
    );
};

export default BiQueue;
