import { createTheme } from '@mui/material/styles';
import {heebo} from "@/theme/fonts";
import {ADMIN_UI_COLORS} from "@/theme/colors";

const theme = createTheme({
    palette: {
        primary: {
            main: '#3E3F41', // 202123Primary color (e.g., sidebar background)
        },
        secondary: {
            main: '#3E3F41', // Secondary color (e.g., hover effect or buttons)
        },
        background: {
            default: '#FFFFFF',  // 202123', Default background color for the app
            paper: '#FFFFFF', // 202123 Background color for components like Drawer
        },
        ...ADMIN_UI_COLORS,
        text: {
            primary: '#444f55', //'#FFFFFF',
            secondary: '#444f55'  //000000',
        },
    },
    typography: {
        fontFamily: heebo.style.fontFamily
    },
    components: {
        MuiOutlinedInput: {
            styleOverrides: {
                root: {
                    '& .MuiOutlinedInput-notchedOutline': {
                        display: 'none',
                    },
                    '&:before': {
                        content: '""',
                        borderBottom: '2px solid #3E3F41',
                        position: 'absolute',
                        bottom: 0,
                        left: 0,
                        right: 0,
                    },
                    '&:hover:before': {
                        borderBottom: '2px solid #FF9600',
                    },
                    '&.Mui-focused:before': {
                        borderBottom: '2px solid #FF9600',
                    },
                }
            },
        },
    },
})

export default theme;
