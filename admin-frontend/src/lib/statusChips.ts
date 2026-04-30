export const statusChips = {
    REQUESTED: "grayLight",
    CREATED: "greenDark",
    ILLEGAL: "depricatedLight",
    NOT_VALID: "pinkLight",
    NO_OVERLAP: "blueLight",
    RESUBSCRIBE: "yellowLight",
    ERROR: "depricatedLight"
} as const;

export const messageTypeChips = {
    DATEX2: "grayLight",
    DENM: "greenLight",
    IVIM: "depricatedLight",
    SPATEM: "pinkLight",
    MAPEM: "blueLight",
    SREM: "yellowLight",
    SSEM: "orangeLight",
    CAM: "purpleLight",
};

export const colorMapping: Record<string, "default" | "primary" | "secondary" | "error" | "info" | "success" | "warning"> = {
    greenDark: "success",
    depricatedLight: "error",
    yellowLight: "warning",
    blueLight: "info",
    pinkLight: "error",
    grayLight: "default",
};
