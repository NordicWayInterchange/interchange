import { useQuery } from "@tanstack/react-query";

const fetchQueueNameExists: (commonName: string, queueName: string
) => Promise<boolean> = async (commonName: string, queueName: string) => {
    const res = await fetch(`/api/${commonName}/queueValidator/${queueName}`);
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

const useFetchQueueNameExists = (commonName: string, queueName: string) => {
    return useQuery({
        queryKey: ["queues"],
        queryFn: () => fetchQueueNameExists(commonName, queueName),
    });
};

export { useFetchQueueNameExists };

