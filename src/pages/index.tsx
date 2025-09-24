import Mainheading from "@/components/shared/typography/Mainheading";
import {Box, Card, Divider, Typography} from "@mui/material";
import {useSession} from "next-auth/react";
import {useFetchNeighbours} from "@/hooks/useFetchNeighbours";
import Link from "next/link";
import React from "react";
import Subheading from "@/components/shared/typography/Subheading";
import {useFetchServiceProviders} from "@/hooks/useFetchServiceProviders";
import {useFetchExchanges} from "@/hooks/useFetchExchanges";
import {useFetchMatchingCapabilities} from "@/hooks/useFetchMatchingCapabilities";
import {useFetchQueues} from "@/hooks/useFetchQueues";
import Loading from "@/components/shared/components/Loading";
import SyncAltIcon from "@mui/icons-material/SyncAlt";
import Groups2Icon from "@mui/icons-material/Groups2";
import ChangeCircleIcon from "@mui/icons-material/ChangeCircle";
import DensitySmallIcon from "@mui/icons-material/DensitySmall";
import AutoGraphIcon from "@mui/icons-material/AutoGraph";

function useAllApplicationData() {
    const {data: session} = useSession();
    const {data: neighbourData, isLoading: isLoadingNeighbour} = useFetchNeighbours(
        session?.user.commonName as string
    );

    const {data: serviceProvidersData, isLoading: isLoadingServiceProvider} = useFetchServiceProviders(
        session?.user.commonName as string
    );

    const {data: exchangeData, isLoading: isLoadingExchange} = useFetchExchanges(
        session?.user.commonName as string
    );

    const {data: matchingCapabilities, isLoading: isLoadingMatchingCapabilities} = useFetchMatchingCapabilities(
        session?.user.commonName as string
    );

    const {data: queuesData, isLoading: isLoadingQueues} = useFetchQueues(
        session?.user.commonName as string
    );
    return {
        session,
        neighbourData,
        isLoadingNeighbour,
        serviceProvidersData,
        isLoadingServiceProvider,
        exchangeData,
        isLoadingExchange,
        matchingCapabilities,
        isLoadingMatchingCapabilities,
        queuesData,
        isLoadingQueues
    };
}

export default function Home() {
    const {
        session,
        neighbourData,
        isLoadingNeighbour,
        serviceProvidersData,
        isLoadingServiceProvider,
        exchangeData,
        isLoadingExchange,
        matchingCapabilities,
        isLoadingMatchingCapabilities,
        queuesData,
        isLoadingQueues
    } = useAllApplicationData();

    const deliveriesWithMatchingCapability = matchingCapabilities?.some(item => item.matches?.length > 0) ?
        (matchingCapabilities.map((item) => (
        item.matches
            .filter(match => match.capabilityMatchApi.length > 0)
            .map((match) => (match))))).reduce((sum, matchResult) => sum + matchResult.length, 0) : 0;

    const capabilitiesCount = serviceProvidersData?.map((item: { capabilities: any; }) => (item.capabilities || [])).reduce((sum: any, capabilities: string | any[]) => (sum + capabilities.length), 0);
    const subscriptionsCount = serviceProvidersData?.map((item: { subscriptions: any; }) => (item.subscriptions || [])).reduce((sum: any, subscriptions: string | any[]) => (sum + subscriptions.length), 0);
    const deliveriesCount = serviceProvidersData?.map((item: { deliveries: any; }) => (item.deliveries || [])).reduce((sum: any, deliveries: string | any[]) => (sum + deliveries.length), 0);
    const privateChannelsCount = serviceProvidersData?.map((item: { privatechannels: any; }) => (item.privatechannels || [])).reduce((sum: any, privatechannels: string | any[]) => (sum + privatechannels.length), 0);
    const privateChannelsPeerCount = serviceProvidersData?.map((item: { privatechannelsPeer: any; }) => (item.privatechannelsPeer || [])).reduce((sum: any, privatechannelsPeer: string | any[]) => (sum + privatechannelsPeer.length), 0);

    const neighbourCapabilitiesCount = neighbourData?.reduce((sum, item) => {
        // eslint-disable-next-line @typescript-eslint/ban-ts-comment
        // @ts-expect-error
        const innerArray = item?.capabilities?.capabilities;
        return sum + (Array.isArray(innerArray) ? innerArray.length : 0);
    }, 0) ?? 0;

    const ourRequestedSubscriptionsCount = neighbourData?.map((item) => (item.ourRequestedSubscriptions || [])).reduce((sum, ourRequestedSubscriptions) => (sum + ourRequestedSubscriptions.subscriptions.length), 0);
    const neighbourRequestedSubscriptionsCount = neighbourData?.map((item) => (item.neighbourRequestedSubscriptions || [])).reduce((sum, neighbourRequestedSubscriptions) => (sum + neighbourRequestedSubscriptions.subscriptions.length), 0);
    const exchangeBindingCount = Array.isArray(exchangeData) ? exchangeData?.map((item) => (item.bindings || [])).reduce((sum, bindings) => (sum + bindings.length), 0) : [] ;
    const locSubqCount = Array.isArray(queuesData)? queuesData.filter(item => item.name.startsWith("loc-")).length : 0;
    const dlqCount = Array.isArray(queuesData) ?queuesData.filter(item => item.name.startsWith("dlq-")).length : 0;

    const shortcuts = [
        {
            icon: <SyncAltIcon />,
            header: 'SERVICE PROVIDERS',
            firstSubValueHeader: 'Capabiltieis',
            secondSubValueHeader: 'Subscriptions',
            thirdSubValueHeader: 'Deliveries',
            fourthSubValueHeader: 'Private channels',
            fifthSubValueHeader: 'Private channel peers',
            url: "/serviceProviders",
            count: serviceProvidersData?.length,
            firstSubValueCount: capabilitiesCount,
            secondSubValueCount: subscriptionsCount,
            thirdSubValueCount: deliveriesCount,
            fourthSubValueCount: privateChannelsCount,
            fifthSubValueCount: privateChannelsPeerCount,
        },
        {
            icon: <Groups2Icon />,
            header: 'NEIGHBOURS',
            firstSubValueHeader: 'Neighbour capabilities',
            secondSubValueHeader: 'Our subscriptions',
            thirdSubValueHeader: 'Neighbour subscriptions',
            url: "/neighbours",
            count: neighbourData?.length,
            firstSubValueCount: neighbourCapabilitiesCount,
            secondSubValueCount: ourRequestedSubscriptionsCount,
            thirdSubValueCount: neighbourRequestedSubscriptionsCount,
        },
        {
            icon: <ChangeCircleIcon />,
            header: 'EXCHANGES',
            firstSubValueHeader: 'Bindings',
            url: "/exchanges",
            count: exchangeData?.length,
            firstSubValueCount: exchangeBindingCount,
        },
        {
            icon: <DensitySmallIcon />,
            header: 'QUEUES',
            url: "/queues",
            count: queuesData?.length,
            firstSubValueHeader: 'Dead letter queues(DLQ)',
            firstSubValueCount: dlqCount,
            secondSubValueHeader: 'Local subscription queues',
            secondSubValueCount: locSubqCount,
        },
        {
            icon: <AutoGraphIcon />,
            header: 'GRAPHS',
            url: "/matchingCapabilitiesGraph",
            firstSubValueHeader: 'Deliveries with matching capabilities',
            firstSubValueCount: deliveriesWithMatchingCapability,
        }
    ];

    if (isLoadingNeighbour || isLoadingServiceProvider || isLoadingNeighbour || isLoadingExchange || isLoadingMatchingCapabilities || isLoadingQueues ) {
        return <Loading text="Dashboard"/>
    }
    return (
        <>
            <Box flex={1}>
                <Mainheading>Welcome, {session?.user?.name}!</Mainheading>
                <Divider sx={{marginY: 3}}/>
                <Subheading>
                    Dashboard
                </Subheading>
                <Box sx={{display: "flex", flexDirection: "column", gap: 3}}>
                    <Box
                        sx={{
                            display: "flex",
                            flexWrap: "wrap"
                        }}
                    >
                        {shortcuts.map((shortcut, key) => (
                            <Link
                                key={key}
                                href={shortcut.url}
                                style={{
                                    textDecoration: "none",
                                    marginRight: 15,
                                    marginTop: 10,
                                }}
                            >
                                <Card
                                    variant="outlined"
                                    sx={{
                                        display: "flex",
                                        flexDirection: "column",
                                        justifyContent: "center",
                                        alignItems: "center",
                                        width: 280,
                                        "&:hover": {
                                            boxShadow: 7,
                                            textDecoration: "underline"
                                        },
                                        borderBottom: "2px solid #FF9600",
                                        height: 280,
                                        boxShadow: 1
                                    }}
                                >
                                    <Box key={key} sx={{ textAlign: 'center', mt:5 }}>
                                        <Box>
                                            <Typography sx={{ fontWeight: 'bold', textDecoration: "underline"}} variant="subtitle1">{shortcut.icon}</Typography>
                                            <Typography sx={{ fontWeight: 'bold', textDecoration: "underline"}} variant="subtitle1">{shortcut.header}</Typography>
                                            <Typography  variant="h6">{shortcut.count}</Typography>
                                        </Box>

                                        <Box sx={{ display: 'inline-flex', justifyContent: 'center', alignItems: 'center', gap: 1, mt: 5}}>
                                            {[{ header: shortcut.firstSubValueHeader, count: shortcut.firstSubValueCount }, { header: shortcut.secondSubValueHeader, count: shortcut.secondSubValueCount },
                                                { header: shortcut.thirdSubValueHeader, count: shortcut.thirdSubValueCount }].map(
                                                (entry, i) => (
                                                    <Box key={i}>
                                                        <Typography sx={{ textDecoration: "underline" }} variant="subtitle2">{entry.header}</Typography>
                                                        <Typography sx={{ fontWeight: 'bold' }}>{entry.count}</Typography>
                                                    </Box>
                                                )
                                            )}
                                        </Box>
                                        <Box sx={{ display: 'inline-flex', justifyContent: 'center', alignItems: 'center', gap: 1}}>
                                            {[{ header: shortcut.fourthSubValueHeader, count: shortcut.fourthSubValueCount }, { header: shortcut.fifthSubValueHeader, count: shortcut.fifthSubValueCount}].map(
                                                (entry, i) => (
                                                    <Box key={i}>
                                                        <Typography noWrap={true} sx={{ textDecoration: "underline" }} variant="subtitle2">{entry.header}</Typography>
                                                        <Typography sx={{ fontWeight: 'bold' }}>{entry.count}</Typography>
                                                    </Box>
                                                )
                                            )}
                                        </Box>
                                    </Box>
                                </Card>
                            </Link>
                        ))}
                    </Box>
                </Box>
            </Box>
        </>
    );
}
