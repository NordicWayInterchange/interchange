import { useQuery } from "@tanstack/react-query";
import {Neighbours} from "@/types/neighbours";

const fetchNeighbours: (
    commonName: string
) => Promise<Neighbours[]> = async (commonName: string) => {
    const res = await fetch(`${commonName}/neighbours`);
    if (res.ok) {
        return res.json();
    } else {
        const errorObj = await res.json();
        throw new Error(`${errorObj.errorCode}: ${errorObj.message}`);
    }
};

const useFetchNeighbours = (commonName: string) => {
    return useQuery({
        queryKey: ["neighbours"],
        queryFn: () => fetchNeighbours(commonName),
    });
};

export { useFetchNeighbours };

