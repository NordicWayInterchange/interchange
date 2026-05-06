import { createTheme } from "@mui/material";
import { heebo } from "@/theme/fonts";
import { INTERCHANGEPORTAL_COLORS } from "@/theme/colors";

export const interchangePortal = createTheme({
  typography: {
    fontFamily: heebo.style.fontFamily,
  },
  palette: {
    ...INTERCHANGEPORTAL_COLORS,
    text: {
      primary: '#444f55',
      secondary: '#444f55'
    }
  },
});
