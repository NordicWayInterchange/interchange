import {
    Box,
    Drawer, IconButton,
    List,
    ListItem, ListItemText,
    Toolbar, Typography
} from "@mui/material";
import React from "react";
import CloseIcon from "@mui/icons-material/Close";
import {drawerStyle, StyledCard, StyledHeaderBox} from "@/components/styles/StyledElements";
import {ControlConnection} from "@/types/neighbours";
import {timeConverter} from "@/lib/timeConverter";
import {StatusCircle} from "@/components/shared/components/StatusCircle";
import Loading from "@/components/shared/components/Loading";

type Props = {
    open: boolean;
    handleMoreClose: () => void;
    controlConnection: ControlConnection;
};

const ControlConnectionDrawer = ({open, handleMoreClose, controlConnection}: Props) => {
    if (!controlConnection) {
        return <Loading text="Control connection is loading"/>;
    }

    const connectionStatus = controlConnection.connectionStatus;
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
                                <Typography> Control connection details</Typography>
                                <Box style={{marginBottom: '10px'}}>
                                    <StatusCircle status={connectionStatus}/>
                                    <span style={{marginLeft: '8px'}}>{connectionStatus}</span>
                                </Box>
                            </StyledHeaderBox>
                        </ListItem>
                        <ListItem>
                            <StyledCard variant="outlined">
                                <Box sx={{display: "flex", justifyContent: "space-between"}}>
                                    <Box>
                                        <ListItemText primary={"ID"} secondary={controlConnection.id}/>
                                    </Box>
                                </Box>
                            </StyledCard>
                        </ListItem>

                        <ListItem>
                            <StyledCard variant="outlined">
                                <Typography>Time</Typography>
                                <Box>
                                    {controlConnection.backoffStart && (<ListItem>
                                            <ListItemText
                                                primary={
                                                    <Typography variant="body2">
                                                        Backoff start: <Typography sx={{component:'span', fontWeight:'bold'}}> {controlConnection.backoffStart ? timeConverter(controlConnection.backoffStart) : ''} </Typography>
                                                    </Typography>
                                                }
                                            />
                                        </ListItem>
                                    )}
                                    <ListItem>
                                        <ListItemText
                                            primary={
                                                <Typography variant="body2">
                                                    Backoff attempts: <Typography sx={{component:'span', fontWeight:'bold'}}> {controlConnection.backoffAttempts} </Typography>
                                                </Typography>
                                            }
                                        />
                                    </ListItem>
                                    {controlConnection.unreachableTime && (<ListItem>
                                            <ListItemText
                                                primary={
                                                    <Typography variant="body2">
                                                        Unreachable time: <Typography sx={{component:'span', fontWeight:'bold'}}> {controlConnection.unreachableTime ? timeConverter(controlConnection.unreachableTime) : ''} </Typography>
                                                    </Typography>
                                                }
                                            />
                                        </ListItem>
                                    )}
                                    {controlConnection.lastFailedConnectionAttempt && (<ListItem>
                                            <ListItemText
                                                primary={
                                                    <Typography variant="body2">
                                                        Last failed Connection
                                                        attempt: <Typography sx={{component:'span', fontWeight:'bold'}}> {controlConnection.lastFailedConnectionAttempt ? timeConverter(controlConnection.lastFailedConnectionAttempt) : ''}
                                                    </Typography>
                                                    </Typography>
                                                }
                                            />
                                        </ListItem>
                                    )}
                                </Box>
                            </StyledCard>
                        </ListItem>
                    </List>
                </Box>
            </Drawer>
        </>
    );
};


export default ControlConnectionDrawer;
