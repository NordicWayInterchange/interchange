import { useQuery } from '@tanstack/react-query';

export default function Home() {
    const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL;

    const fetcher = async (url: string) => {
        console.log(`${backendUrl}${url}`)
        const response = await fetch(`${backendUrl}${url}`);
        return response.json();
    };

    const endpoint = '/admin/test'
    const { data, error, isLoading } = useQuery({
        queryKey: ['myData'],
        queryFn: () => fetcher(`${endpoint}`),
    });

    if (isLoading) return <div>Loading...</div>;
    if (error instanceof Error) return <div>Error: {error.message}</div>;

    return (
        <div>
            <h1>Fetched Data:</h1>
            <pre>{JSON.stringify(data, null, 2)}</pre>
        </div>
    );
}
