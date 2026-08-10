import {useSession} from "next-auth/react";
import {Typography} from "@mui/material";
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import {useFetchExchangeNameExists} from "@/hooks/useFetchExchangeNameExists";
import Loading from "@/components/shared/components/Loading";
import {validatorStyle} from "@/components/styles/StyledElements";
import NextLink from 'next/link';
import {Box} from "@mui/system";

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
                <Typography sx={{color:'success.main', marginTop:2}} style={validatorStyle }>
                    <CheckCircleIcon style={{ color: 'green', marginRight: 8 }} /> &quot;{exchangeName}&quot; exchange exists!
                </Typography>
            ) : (
                <Typography sx={{color:'success.main', marginTop:2}} style={validatorStyle}>
                    <Box sx={{ display: "flex", alignItems: "center" }}>
                    <WarningAmberIcon style={{ color: 'orange', marginRight: 8 }} /> &quot;{exchangeName}&quot; exchange could not be found!
                    <NextLink href="/matchingCapabilitiesGraph" passHref>
                        <Typography noWrap
                            sx={{
                                display: "flex",
                                alignItems: "center",
                                textDecoration: "none",
                                color: "#444f55", cursor: "pointer",
                                mr: 1
                            }}
                        >
                            Display graph
                        </Typography>
                    </NextLink>
                    </Box>
                </Typography>
            )}
        </div>
    );
}