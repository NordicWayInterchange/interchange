import {Typography} from "@mui/material";
import {VERSION} from "../../../generated/version";

export default function Version() {
return (
    <>
        <Typography
            variant="caption"
            sx={{
                position: "fixed",
                bottom: 8,
                left: 12,
                fontSize: "0.75em",
                opacity: .85
            }}
        >
            Version: {VERSION}
        </Typography>
    </>
)
}