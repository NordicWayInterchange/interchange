import {styled} from "@mui/system";
import {Button} from "@mui/material";

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