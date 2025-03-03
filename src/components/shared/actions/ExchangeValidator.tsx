import {useSession} from "next-auth/react";
import {CircularProgress, Typography} from "@mui/material";
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import {useFetchExchangeNameExists} from "@/hooks/useFetchExchangeNameExists";
import {Box} from "@mui/system";

type Props = {
    exchangeName: string;
};

export default function ExchangeValidator ({ exchangeName }: Props) {
    const {data: session} = useSession();
    const {data: exchangeValidator} = useFetchExchangeNameExists(
        session?.user.commonName as string, exchangeName
    );

    return (
        <div>
            {!exchangeValidator ? (
                <Box
                    display="flex"
                    justifyContent="center"
                    alignItems="center"
                    flexDirection="column"
                >
                    <CircularProgress />
                    <Typography fontWeight="bold" color="textSecondary" marginTop={2}>
                        Exchange is loading...
                    </Typography>
                </Box>
            ) : exchangeValidator ? (
                <Typography color="success.main" marginTop={2} style={{ display: 'flex', alignItems: 'center', fontWeight: 'bold' }}>
                    <CheckCircleIcon style={{ color: 'green', marginRight: 8 }} /> &quot;{exchangeName}&quot; exchange exists!
                </Typography>
            ) : (
                <Typography color="warning.main" marginTop={2} style={{ display: 'flex', alignItems: 'center', fontWeight: 'bold' }}>
                    <WarningAmberIcon style={{ color: 'orange', marginRight: 8 }} /> &quot;{exchangeName}&quot; exchange could not be found!
                </Typography>
            )}
        </div>
    );
}