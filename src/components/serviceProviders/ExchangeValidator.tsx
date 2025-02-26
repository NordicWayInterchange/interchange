import {useSession} from "next-auth/react";
import {Typography} from "@mui/material";
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import {useFetchExchangeNameExists} from "@/hooks/useFetchExchangeNameExists";

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
            {exchangeValidator === null ? (
                <Typography>Loading...</Typography>
            ) : exchangeValidator ? (
                <Typography color="success.main" style={{ display: 'flex', alignItems: 'center', fontWeight: 'bold' }}>
                    <CheckCircleIcon style={{ color: 'green', marginRight: 8 }} /> &quot;{exchangeName}&quot; exchange exists!
                </Typography>
            ) : (
                <Typography color="warning.main" style={{ display: 'flex', alignItems: 'center', fontWeight: 'bold' }}>
                    <WarningAmberIcon style={{ color: 'orange', marginRight: 8 }} /> &quot;{exchangeName}&quot; exchange could not be found!
                </Typography>
            )}
        </div>
    );
}