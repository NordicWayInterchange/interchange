import {
    Box,
    Card,
    Drawer, FormControl, IconButton, InputAdornment,
    List,
    ListItem, ListItemText, TextField,
    Toolbar, Typography
} from "@mui/material";
import React, {useState} from "react";
import {useSession} from "next-auth/react";
import CloseIcon from "@mui/icons-material/Close";
import {timeConverter} from "@/lib/timeConverter";
import {styled} from "@mui/material/styles";
import {drawerStyle} from "@/components/styles/StyledElements";
import {ContentCopy} from "@/components/shared/actions/ContentCopy";
import neighbours from "@/pages/neighbours";

type Props = {
    capabilities: neighbours;
    open: boolean;
    handleMoreClose: () => void;
};

const CapabilityDrawer = ({capabilities, open, handleMoreClose}: Props) => {
    const [dialogOpen, setDialogOpen] = useState<boolean>(false);
    const {data: session} = useSession();
    console.log(capabilities)
    const handleClickClose = (close: boolean) => {
        setDialogOpen(close);
    };

    const getAttribute = () => {
        // return label == "Delivery" ? capabilities.endpoints[0].target: capabilities.endpoints[0].source;
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
            <Toolbar/>
            <Box sx={{padding: 1}}>
                <List>
                    <ListItem sx={{justifyContent: "flex-end"}}>
                        <IconButton onClick={handleMoreClose}>
                            <CloseIcon/>
                        </IconButton>
                    </ListItem>
                    <ListItem>
                        <StyledCard variant={"outlined"}>
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
            </List>
        </Box>
        </Drawer>
</>
);
};


const StyledCard = styled(Card)(({}) => ({
    padding: "16px",
        width
:
    "100%",
}));

const StyledHeaderBox = styled(Box)(({}) => ({
    display: "flex",
        alignItems
:
    "center",
        justifyContent
:
    "space-between",
        width
:
    "100%",
}));


export default CapabilityDrawer;
