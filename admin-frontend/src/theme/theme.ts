import { createTheme } from '@mui/material/styles';
import {heebo} from "@/theme/fonts";
import {ADMIN_UI_COLORS} from "@/theme/colors";

const theme = createTheme({
    palette: {
        primary: {
            main: '#3E3F41',
        },
        secondary: {
            main: '#3E3F41',
        },
        background: {
            default: '#FFFFFF',
            paper: '#FFFFFF',
        },
        ...ADMIN_UI_COLORS,
        text: {
            primary: '#444f55',
            secondary: '#444f55'
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
