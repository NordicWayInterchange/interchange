import { createTheme } from '@mui/material/styles';
import {heebo} from "@/theme/fonts";
import {ADMIN_UI_COLORS} from "@/theme/colors";

const theme = createTheme({
    palette: {
        primary: {
            main: '#FFFFFF', // 202123Primary color (e.g., sidebar background)
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
        MuiTextField: {
            styleOverrides: {
                root: {
                    '& .MuiInput-underline:before': {
                        borderBottom: '2px solid #000',
                    },
                    '& .MuiInput-underline:hover:before': {
                        borderBottom: '2px solid #000',
                    },
                    '& .MuiInput-underline:after': {
                        borderBottom: '2px solid #000',
                    },
                    '& .MuiOutlinedInput-notchedOutline': {
                        border: 'none',
                    },
                },
            },
        },
    },
});

export default theme;
