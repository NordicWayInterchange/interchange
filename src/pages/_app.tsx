import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import {useState} from 'react';
import {SessionProvider} from "next-auth/react";
import {ThemeProvider} from "@mui/material";
import theme from "@/theme/theme";
import Layout from "@/components/shared/components/Layout";
import type { AppProps } from "next/app";
import {ReactQueryDevtools} from "@tanstack/react-query-devtools";

export default function App({
                                Component,
                                pageProps: {session, ...pageProps},
                            }: AppProps) {
    const [queryClient] = useState(() => new QueryClient());

    return (
        <SessionProvider session={session}>
            <QueryClientProvider client={queryClient}>
                <ThemeProvider theme={theme}>
                    <Layout>
                        <Component {...pageProps} />
                    </Layout>
                </ThemeProvider>
                <ReactQueryDevtools />
            </QueryClientProvider>
        </SessionProvider>
    );
}
