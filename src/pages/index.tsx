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
        (matchingCapabilities.map((item, index) => (
        item.matches
            .filter(match => match.capabilityMatchApi.length > 0)
            .map((match, matchIndex) => (match))))).reduce((sum, matchResult) => sum + matchResult.length, 0) : 0;


    const shortcuts = [
        {
            header: 'SERVICE PROVIDERS',
            url: "/serviceProviders",
            count: serviceProvidersData?.length
        },
        {
            header: 'NEIGHBOURS',
            url: "/neighbours",
            count: neighbourData?.length,
        },
        {
            header: 'EXCHANGES',
            url: "/exchanges",
            count: exchangeData?.length,
        },
        {
            header: 'QUEUES',
            url: "/queues",
            count: queuesData?.length,
        },
        {
            header: 'DELIVERIES WITH MATCHING CAPABILITIES',
            url: "/matchingCapabilitiesGraph",
            count: deliveriesWithMatchingCapability,
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
                                        width: 170,
                                        "&:hover": {
                                            boxShadow: 7,
                                            textDecoration: "underline"
                                        },
                                        borderBottom: "2px solid #FF9600",
                                        height: 150,
                                        boxShadow: 1
                                    }}
                                >
                                    <Box>
                                        <Typography sx={{fontWeight: 500, textAlign: 'center'}}>
                                            {shortcut.count}<br />{shortcut.header}
                                        </Typography>
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
