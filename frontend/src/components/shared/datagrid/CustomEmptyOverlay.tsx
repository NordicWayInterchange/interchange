import {styled} from "@mui/material/styles";
import React from "react";
import {Box} from "@mui/system";
import SentimentNeutralIcon from "@mui/icons-material/SentimentNeutral";

export const CustomEmptyOverlayMatching = () => {
    return (
        <StyledGridOverlay>
            <StyledBox>
                <SentimentNeutralIcon fontSize="medium" color="disabled" sx={{mt: -0.5}}/>
                No matching capabilities for the specified input
            </StyledBox>
        </StyledGridOverlay>
    );
};

export const CustomEmptyOverlaySubscription = () => {
    return (
        <StyledGridOverlay>
            <StyledBox>
                <SentimentNeutralIcon fontSize="medium" color="disabled" sx={{mt: -0.5}}/>
                Could not find any subscriptions, try creating one!
            </StyledBox>
        </StyledGridOverlay>
    );
};

export const CustomEmptyOverlayCapabilites = () => {
    return (
        <StyledGridOverlay>
            <StyledBox>
                <SentimentNeutralIcon fontSize="medium" color="disabled" sx={{mt: -0.5}}/>
                Could not find any capabilities!
            </StyledBox>
        </StyledGridOverlay>
    );
};

export const CustomEmptyOverlayDeliveries = () => {
    return (
        <StyledGridOverlay>
            <StyledBox>
                <SentimentNeutralIcon fontSize="medium" color="disabled" sx={{mt: -0.5}}/>
                Could not find any deliveries, try creating one!
            </StyledBox>
        </StyledGridOverlay>
    );
};

export const CustomEmptyOverlayUserCapabilites = () => {
    return (
        <StyledGridOverlay>
            <StyledBox>
                <SentimentNeutralIcon fontSize="medium" color="disabled" sx={{mt: -0.5}}/>
                Could not find any user capabilities, try creating one!
            </StyledBox>
        </StyledGridOverlay>
    );
};

export const CustomEmptyOverlayPrivateChannels = () => {
    return (
        <StyledGridOverlay>
            <StyledBox>
                <SentimentNeutralIcon fontSize="medium" color="disabled" sx={{mt: -0.5}}/>
                Could not find any private channels, try creating one!
            </StyledBox>
        </StyledGridOverlay>
    );
};

export const CustomEmptyOverlayBiqueueEndpoints = () => {
    return (
        <StyledGridOverlay>
            <StyledBox>
                <SentimentNeutralIcon fontSize="medium" color="disabled" sx={{mt: -0.5}}/>
                Could not find any bi-queue endpoints!
            </StyledBox>
        </StyledGridOverlay>
    );
};

const StyledGridOverlay = styled("div")(({}) => ({
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    justifyContent: "center",
    height: "100%"
}));

const StyledBox = styled(Box)(({}) => ({
    display: "flex",
    alignItems: "center",
    gap: "8px"
}));
