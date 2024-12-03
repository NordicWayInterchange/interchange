import {QueryClient, QueryClientProvider} from '@tanstack/react-query';
import type {AppProps} from 'next/app';
import {useState} from 'react';
import {SessionProvider} from "next-auth/react";
import {ThemeProvider} from "@mui/material";
import theme from "@/theme/theme";

export default function App({
                                Component,
                                pageProps: {session, ...pageProps},
                            }: AppProps) {
    const [queryClient] = useState(() => new QueryClient());

    return (
        <SessionProvider session={session}>
            <QueryClientProvider client={queryClient}>
                <ThemeProvider theme={theme}>
                    <Component {...pageProps} />
                </ThemeProvider>
            </QueryClientProvider>
        </SessionProvider>
    );
}
