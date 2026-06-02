import { useQuery } from "@tanstack/react-query";
import {Exchanges} from "@/types/exchanges";

const fetchExchanges: (
    commonName: string
) => Promise<Exchanges[]> = async (commonName: string) => {
    const res = await fetch(`/api/${commonName}/exchanges`);
    if (res.ok) {
        return res.json();
    } else {
        const errorObj = await res.json();
        throw new Error(`${errorObj.errorCode}: ${errorObj.message}`);
    }
};

const useFetchExchanges = (commonName: string) => {
    return useQuery({
        queryKey: ["exchanges"],
        queryFn: () => fetchExchanges(commonName),
    });
};

export { useFetchExchanges };

