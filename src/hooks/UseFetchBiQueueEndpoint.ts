import {useQuery} from "@tanstack/react-query";
import {BiQueueEndpointResponse} from "@/types/BiQueueResponse";

const fetchBiQueueEndpoint: (
    commonName: string
) => Promise<BiQueueEndpointResponse> = async (commonName) => {
    const res = await fetch(`/api/${commonName}/biqueueendpoint`);
    if (res.ok) {
        return res.json();
    } else {
        const errorObj = await res.json();
        throw new Error(`${errorObj.errorCode}: ${errorObj.message}`);
    }
};

const useFetchBiQueueEndpoint = (
    commonName: string
) => {
    return useQuery({
        queryKey: ["biqueueendpoint"],
        queryFn: () => fetchBiQueueEndpoint(commonName),
    });
};

export { useFetchBiQueueEndpoint };