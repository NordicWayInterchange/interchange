import { createTheme } from "@mui/material";
const { palette } = createTheme();

const { augmentColor } = palette;
const createColor = (mainColor: any) =>
    augmentColor({ color: { main: mainColor } });

export const SHARED_COLORS = {
    depricatedRed: createColor("#F8DEDE"),
    grayDark: createColor("#444f55"),
    grayLight: createColor("#DADADA"),
    depricatedDark: createColor("#7E1010"),
    depricatedLight: createColor("#F8DEDE"),
    greenDark: createColor("#1D7721"),
    greenLight: createColor("#E8F3E9"),
    blueDark: createColor("#006C9A"),
    blueLight: createColor("#D4F7FF"),
    yellowDark: createColor("#A17E00"),
    yellowLight: createColor("#FFF5C8"),
    pinkDark: createColor("#9C176F"),
    pinkLight: createColor("#EDCEF5"),
    orangeDark: createColor("#FF9600"), //vegvesen oransje
    orangeLight: createColor("#ffbf7d"),
    purpleDark: createColor("#7255c0"),
    purpleLight: createColor("#c1aaff"),
    redLight: createColor("#B63434")
};
export const ADMIN_UI_COLORS = {
    menuHoverColor: '#3E3F41',
    mainBackgroundColor: '#2B2B2B',
    iconColor: '#A9B7C6',
    ...SHARED_COLORS,
};
