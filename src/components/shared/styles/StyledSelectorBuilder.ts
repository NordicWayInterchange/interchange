import { styled } from "@mui/material/styles";
import { Box, Button, Card, FormControl } from "@mui/material";

const width = 600;

export const StyledFormControl = styled(FormControl)(({}) => ({
  display: "flex",
  flexDirection: "column",
  gap: "24px",
}));

export const StyledButton = styled(Button)(({}) => ({
  textTransform: 'none',
  marginLeft: 2,
  height: 40,
  width: '200px',
  borderColor: 'buttonThemeColor',
  paddingBottom: '5px',
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

export const StyledCard = styled(Card)(({}) => ({
  padding: "16px",
  width: "100%",
}));

export const menuItemStyles = {
  '&:hover': {
    backgroundColor: 'menuItemHoverColor',
  },
  '&.Mui-selected': {
    backgroundColor: 'menuItemBackgroundColor',
    '&:hover': {
      backgroundColor: 'menuItemHoverColor',
    },
  }
};

export const drawerStyle = {
  width: width,
  flexShrink: 0,
  "& .MuiDrawer-paper": {
    width: width,
    boxSizing: "border-box",
  },
}
