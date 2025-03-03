import {useSession} from "next-auth/react";
import {Typography} from "@mui/material";
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import {useFetchExchangeNameExists} from "@/hooks/useFetchExchangeNameExists";
import Loading from "@/components/shared/components/Loading";

type Props = {
    exchangeName: string;
};

export default function ExchangeValidator ({ exchangeName }: Props) {
    const {data: session} = useSession();
    const {data, error} = useFetchExchangeNameExists(
        session?.user.commonName as string, exchangeName
    );
    type DataType = boolean | null | { description: string } | undefined;

    const isDataWithDescription = (data: DataType): data is { description: string } => {
        return typeof data === 'object' && data !== null && 'description' in data;
    };

    return (
        <div>
            {(data === undefined || data === null || isDataWithDescription(data) && data.description === "Page not found" || error) ? (
                <Loading text="Exchange"/>
            ) : data ? (
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