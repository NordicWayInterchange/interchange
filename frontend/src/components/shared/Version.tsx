import {Typography} from "@mui/material";
import {VERSION} from "../../../public/generated/version";

export default function Version() {
return (
    <>
        <Typography
            variant="caption"
            sx={{
                position: "fixed",
                bottom: 8,
                left: 12,
                fontSize: "0.63em",
                opacity: .85
            }}
        >
            Version: {VERSION}
        </Typography>
    </>
)
}