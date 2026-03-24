import { createTheme } from "@mui/material";
import { heebo } from "@/theme/fonts";
import { TRANSPORTPORTAL_COLORS } from "@/theme/colors";

export const transportportal = createTheme({
  typography: {
    fontFamily: heebo.style.fontFamily,
  },
  palette: {
    ...TRANSPORTPORTAL_COLORS,
    text: {
      primary: '#444f55',
      secondary: '#444f55'
    }
  },
});
