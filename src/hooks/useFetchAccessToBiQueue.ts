import { useQuery } from "@tanstack/react-query";
import {BiQueueResponse} from "@/types/BiQueueResponse";

const fetchAccessToBiQueue: (
    commonName: string
) => Promise<BiQueueResponse> = async (commonName: string) => {
    const res = await fetch(`/api/${commonName}/biconsumer`);
    if (res.ok) {
        return res.json();
    } else {
        const errorObj = await res.json();
        throw new Error(`${errorObj.errorCode}: ${errorObj.message}`);
    }
};

const useFetchAccessToBiQueue = (commonName: string) => {
    return useQuery({
        queryKey: ["biconsumer"],
        queryFn: () => fetchAccessToBiQueue(commonName),
    });
};

export { useFetchAccessToBiQueue };

