import { createTheme } from '@mui/material/styles';
import {heebo} from "@/theme/fonts";
import {ADMIN_UI_COLORS} from "@/theme/colors";

const theme = createTheme({
    palette: {
        primary: {
            main: '#202123', // Primary color (e.g., sidebar background)
        },
        secondary: {
            main: '#3E3F41', // Secondary color (e.g., hover effect or buttons)
        },
        background: {
            default: '#202123', // Default background color for the app
            paper: '#202123', // Background color for components like Drawer
        },
        ...ADMIN_UI_COLORS,
        text: {
            primary: '#FFFFFF',
            secondary: '#000000',
        },
    },
    typography: {
        fontFamily: heebo.style.fontFamily
    },
});

export default theme;
