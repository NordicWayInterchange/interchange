import {useQuery} from "@tanstack/react-query";
import {BiQueueResponse} from "@/types/BiQueueResponse";

const fetchBiQueueEndpoints: (
    commonName: string
) => Promise<BiQueueResponse[]> = async (commonName) => {
    const res = await fetch(`/api/${commonName}/biqueueendpoints`);
    if (res.ok) {
        return res.json();
    } else {
        const errorObj = await res.json();
        throw new Error(`${errorObj.errorCode}: ${errorObj.message}`);
    }
};

const useFetchBiQueueEndpoints = (
    commonName: string
) => {
    return useQuery({
        queryKey: ["biqueueendpoints"],
        queryFn: () => fetchBiQueueEndpoints(commonName),
    });
};

export { useFetchBiQueueEndpoints };