import { useQuery } from "@tanstack/react-query";
import {queues} from "@/types/queues";

const fetchQueues: (
    commonName: string
) => Promise<queues[]> = async (commonName: string) => {
    const res = await fetch(`/api/${commonName}/queues`);
    if (res.ok) {
        return res.json();
    } else {
        const errorObj = await res.json();
        throw new Error(`${errorObj.errorCode}: ${errorObj.message}`);
    }
};

const useFetchQueues = (commonName: string) => {
    return useQuery({
        queryKey: ["queues"],
        queryFn: () => fetchQueues(commonName),
    });
};

export { useFetchQueues };

