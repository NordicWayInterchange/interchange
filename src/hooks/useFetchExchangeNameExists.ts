import { useQuery } from "@tanstack/react-query";

const fetchExchangeNameExists: (commonName: string, exchangeName: string
) => Promise<boolean> = async (commonName: string, exchangeName: string) => {
    const res = await fetch(`/api/${commonName}/exchangeValidator/${exchangeName}`);
    if (res.ok) {
        const result = await res.json();
        if (typeof result === 'boolean') {
            return result;
        } else {
            throw new Error("Unexpected response format. Expected a boolean.");
        }
    } else {
        const errorObj = await res.json();
        throw new Error(`${errorObj.errorCode}: ${errorObj.message}`);
    }
};

const useFetchExchangeNameExists = (commonName: string, exchangeName: string) => {
    return useQuery({
        queryKey: ["exchanges"],
        queryFn: () => fetchExchangeNameExists(commonName, exchangeName),
    });
};

export { useFetchExchangeNameExists };

