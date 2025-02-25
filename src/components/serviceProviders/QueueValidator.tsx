import {useSession} from "next-auth/react";
import {useFetchQueueNameExists} from "@/hooks/useFetchQueueNameExists";

type Props = {
    queueName: string;
};

export default function QueueValidator ({ queueName }: Props) {
    const {data: session} = useSession();
    const {data: neighbourData} = useFetchQueueNameExists(
        session?.user.commonName as string, queueName
    );
}