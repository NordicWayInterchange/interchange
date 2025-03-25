import {useQuery} from "@tanstack/react-query";
import {ServiceProviders} from "@/types/serviceProviders";

const fetchPrivateChannels: (
    commonName: string
) => Promise<ServiceProviders[]> = async (commonName: string) => {
    const res = await fetch(`/api/${commonName}/serviceProviders`);
    if (res.ok) {
        return res.json();
    } else {
        const errorObj = await res.json();
        throw new Error(`${errorObj.errorCode}: ${errorObj.message}`);
    }
};

const useFetchPrivateChannels = (commonName: string) => {
    return useQuery({
        queryKey: ["serviceproviderPrivateChannels"],
        queryFn: () => fetchPrivateChannels(commonName),
    });
};

export { useFetchPrivateChannels };