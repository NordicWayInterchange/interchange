import Mainheading from "@/components/shared/typography/Mainheading";
import {Box, Card, Divider, Typography} from "@mui/material";
import {useSession} from "next-auth/react";
import {useFetchNeighbours} from "@/hooks/useFetchNeighbours";
import Link from "next/link";
import React from "react";
import Subheading from "@/components/shared/typography/Subheading";
import {useFetchServiceProviders} from "@/hooks/useFetchServiceProviders";

export default function Home() {
    const {data: session} = useSession();
    const {data: neighbourData} = useFetchNeighbours(
        session?.user.commonName as string
    );

    const {data: serviceProvidersData} = useFetchServiceProviders(
        session?.user.commonName as string
    );

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
        }
    ];

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
