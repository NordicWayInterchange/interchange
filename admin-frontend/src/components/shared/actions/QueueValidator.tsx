import {useSession} from "next-auth/react";
import {useFetchQueueNameExists} from "@/hooks/useFetchQueueNameExists";
import {Typography} from "@mui/material";
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import Loading from "@/components/shared/components/Loading";
import {validatorStyle} from "@/components/styles/StyledElements";

type Props = {
    queueName: string;
};

export default function QueueValidator ({ queueName }: Props) {
    const {data: session} = useSession();
    const {data, error} = useFetchQueueNameExists(
        session?.user.commonName as string, queueName
    );

    type DataType = boolean | null | { description: string } | undefined;

    const isDataWithDescription = (data: DataType): data is { description: string } => {
        return typeof data === 'object' && data !== null && 'description' in data;
    };

    return (
        <div>
            {(data === undefined || data === null || isDataWithDescription(data) && data.description === "Page not found" || error) ? (
                <Loading text="Queue"/>
            ) : data ? (
                <Typography sx={{color:'success.main', marginTop:2}} style={validatorStyle}>
                    <CheckCircleIcon style={{ color: 'green', marginRight: 8 }} /> &quot;{queueName}&quot; queue exists!
                </Typography>
            ) : (
                <Typography sx={{color:'success.main', marginTop:2}} style={validatorStyle}>
                    <WarningAmberIcon style={{ color: 'orange', marginRight: 8 }} /> &quot;{queueName}&quot; queue could not be found!
                </Typography>
            )}
        </div>
    );
}