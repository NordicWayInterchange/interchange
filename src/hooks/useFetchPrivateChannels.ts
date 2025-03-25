import {useQuery} from "@tanstack/react-query";
import {ExtendedServiceProviders, ServiceProviderPrivateChannels, ServiceProviders} from "@/types/serviceProviders";

// @ts-expect-error
const fetchPrivateChannels: (
    commonName: string
) => Promise<ExtendedServiceProviders[]> = async (commonName: string) => {
    const res = await fetch(`/api/${commonName}/serviceproviders`);
    if (res.ok) {
        const serviceProviders: ServiceProviders[] = await res.json();
        const serviceProvidersName = serviceProviders.map(async (sp) => {
            console.log('sp.name', sp.name)
            const fetchServiceProviderPrivateChannels = await fetch(
                `/api/${commonName}/${sp.name}/privateChannels`
            );
            if (fetchServiceProviderPrivateChannels.ok) {
                const data = await fetchServiceProviderPrivateChannels.json();
                return { privateChannels: data };
            } else {
                console.error(
                    `error when fetching ${sp.name} - ${fetchServiceProviderPrivateChannels.status} - ${fetchServiceProviderPrivateChannels.statusText}`
                );
                return {privateChannels: 0 };
            }
        });
        return Promise.all(serviceProvidersName);
    } else {
        const errorObj = await res.json();
        throw new Error(`${errorObj.status}: ${errorObj.error}`);
    }
};

const useFetchPrivateChannels = (commonName: string) => {
    return useQuery({
        queryKey: ["serviceproviderPrivateChannels"],
        queryFn: () => fetchPrivateChannels(commonName),
    });
};

export { useFetchPrivateChannels };