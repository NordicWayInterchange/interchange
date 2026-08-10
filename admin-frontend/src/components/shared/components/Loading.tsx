import {CircularProgress, Typography} from "@mui/material";
import {Box} from "@mui/system";

interface Props  {
    text: string;
}

const Loading = ({ text }: Props) => {
    return (
        <Box
            sx={{
            display:'flex',
            justifyContent:'center',
            alignItems:'center',
            flexDirection:'column'
            }}
        >
            <CircularProgress />
            <Typography sx={{fontWeight:'bold', color:'textSecondary', marginTop:2}}>
                {text} is loading...
            </Typography>
        </Box>
    );
};

export default Loading;
