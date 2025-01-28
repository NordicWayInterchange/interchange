import {useQuery} from "@tanstack/react-query";
import {ServiceProviders} from "@/types/serviceProviders";

const fetchServiceProviders: (
    commonName: string
) => Promise<ServiceProviders[]> = async (commonName: string) => {
    const res = await fetch(`/api/${commonName}/serviceproviders`);
    if (res.ok) {
        return res.json();
    } else {
        const errorObj = await res.json();
        throw new Error(`${errorObj.errorCode}: ${errorObj.message}`);
    }
};

const useFetchServiceProviders = (commonName: string) => {
    return useQuery({
        queryKey: ["serviceproviders"],
        queryFn: () => fetchServiceProviders(commonName),
    });
};

export { useFetchServiceProviders };