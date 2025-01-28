import {styled} from "@mui/system";
import {Box} from "@mui/material";
import SentimentNeutralIcon from "@mui/icons-material/SentimentNeutral";

export const CustomEmptyOverlayNeighbours = () => {
    return (
        <StyledGridOverlay>
            <StyledBox>
                <SentimentNeutralIcon fontSize="medium" color="disabled" sx={{ mt: -0.5 }} />
                Could not find any neighbours in the network!
            </StyledBox>
        </StyledGridOverlay>
    );
};

export const CustomEmptyOverlayServiceProviders = () => {
    return (
        <StyledGridOverlay>
            <StyledBox>
                <SentimentNeutralIcon fontSize="medium" color="disabled" sx={{ mt: -0.5 }} />
                Could not find any service providers in the network!
            </StyledBox>
        </StyledGridOverlay>
    );
};

export const CustomEmptyOverlay = () => {
    return (
        <StyledGridOverlay>
            <StyledBox>
                <SentimentNeutralIcon fontSize="medium" color="disabled" sx={{ mt: -0.5 }} />
                Could not find any data!
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
