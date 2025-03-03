import {useSession} from "next-auth/react";
import {useFetchQueueNameExists} from "@/hooks/useFetchQueueNameExists";
import {CircularProgress, Typography} from "@mui/material";
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import {Box} from "@mui/system";

type Props = {
    queueName: string;
};

export default function QueueValidator ({ queueName }: Props) {
    const {data: session} = useSession();
    const {data: queueValidator} = useFetchQueueNameExists(
        session?.user.commonName as string, queueName
    );

    return (
        <div>
            {!queueValidator ? (
                <Box
                    display="flex"
                    justifyContent="center"
                    alignItems="center"
                    flexDirection="column"
                >
                    <CircularProgress />
                    <Typography fontWeight="bold" color="textSecondary" marginTop={2}>
                        Queue is loading...
                    </Typography>
                </Box>
            ) : queueValidator ? (
                <Typography color="success.main" marginTop={2} style={{ display: 'flex', alignItems: 'center', fontWeight: 'bold'}}>
                    <CheckCircleIcon style={{ color: 'green', marginRight: 8 }} /> &quot;{queueName}&quot; queue exists!
                </Typography>
            ) : (
                <Typography color="warning.main" marginTop={2} style={{ display: 'flex', alignItems: 'center', fontWeight: 'bold'}}>
                    <WarningAmberIcon style={{ color: 'orange', marginRight: 8 }} /> &quot;{queueName}&quot; queue could not be found!
                </Typography>
            )}
        </div>
    );
}