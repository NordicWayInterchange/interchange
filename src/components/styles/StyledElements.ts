import {styled} from "@mui/system";
import {Box, Button, Card, CardProps} from "@mui/material";

const width = 600;


export const StyledButton = styled(Button)(({}) => ({
    textTransform: 'none',
    marginLeft: 2,
    height: 40,
    width: '200px',
    borderColor: 'buttonThemeColor',
    paddingBottom: '10px',
    position: 'relative',
    '&:hover': {
        '&::after': {
            content: '""',
            position: 'absolute',
            bottom: 0,
            left: 0,
            width: '100%',
            height: '3px',
            backgroundColor: '#FF9600',
        },
    },
}));

export const drawerStyle = {
    width: width,
    flexShrink: 0,
    "& .MuiDrawer-paper": {
        width: width,
        boxSizing: "border-box",
    },
}

export const StyledHeaderBox = styled(Box)(({}) => ({
    display: "flex",
    alignItems: "center",
    justifyContent: "space-between",
    width: "100%",
}));

export const StyledCard = styled(Card)<CardProps>(() => ({
    padding: '16px',
    width: '100%',
}));

export const StyledBorderlineSpan = styled("span")({
    textDecoration: "underline",
    fontWeight: "bold",
});

export const validatorStyle = {
    display: 'flex',
    alignItems: 'center',
    fontWeight: 'bold'
}

export const StyledTableHeader = {
    height: 400,
    width: '100%',
    '& .highlighted-cell': {
        backgroundColor: '#ffbf7d',
    },
    '& .custom-header': {
        backgroundColor: '#444F55 !important',
        color: '#ffbf7d',
        fontWeight: 'bold',
    },
}

