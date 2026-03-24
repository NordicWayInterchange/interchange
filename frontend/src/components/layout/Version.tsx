import {Typography} from "@mui/material";

export default function Version() {
return (
    <>
        <Typography
            variant="caption"
            sx={{
                position: "fixed",
                bottom: 8,
                left: 12,
                fontSize: "0.85em",
            }}
        >
            version
        </Typography>
    </>
)
}