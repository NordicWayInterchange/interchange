import type {
    GetServerSidePropsContext
} from "next";
import { signIn } from "next-auth/react";
import { getServerSession } from "next-auth/next";
import { authOptions } from "./api/auth/[...nextauth]";
import { Card, Typography} from "@mui/material";
import * as React from "react";
import {Box} from "@mui/system";
import AdminPanelSettingsIcon from '@mui/icons-material/AdminPanelSettings';
import {StyledButton} from "@/components/styles/StyledElements";

export default function Login(isKeycloak: any) {

    return (
        <Card
            variant="outlined"
            sx={{
                display: "flex",
                flexDirection: "column",
                padding: 5,
                gap: 3,
                width: "500px",
            }}
        >
            <Box sx={{ alignSelf: "center" }}>
            <AdminPanelSettingsIcon sx={{ fontSize: 100 }} />
            </Box>
            <Typography variant="body1">
                Access to this application is restricted to authorized users only. If
                you believe you should have access, please send an email to
                christian.berg.skjetne@vegvesen.no requesting access.
            </Typography>

            <Typography variant="body1">
                Sign in will redirect you to our authentication provider.
            </Typography>

            <div>
                {isKeycloak.isKeycloak ? (
                    <StyledButton
                        variant="contained"
                        sx={{textTransform: "none", width: 250, alignSelf: "center", ml: 10, color:"buttonThemeColor"}}
                        onClick={() => signIn("keycloak")}
                    >
                        <Typography>Sign in with keycloak</Typography>
                    </StyledButton>

                ) : (
                    <StyledButton
                        variant="contained"
                        sx={{textTransform: "none", width: 250, alignSelf: "center", ml: 10, color:"buttonThemeColor"}}
                        onClick={() => signIn("auth0")}
                    >
                        <Typography>Sign in with auth0</Typography>
                    </StyledButton>
                )}
            </div>
        </Card>
    );
}

export async function getServerSideProps(context: GetServerSidePropsContext) {
    const session = await getServerSession(context.req as any, context.res as any, authOptions as any);

    if (session) {
        return { redirect: { destination: "/" } };
    }

    return {
        props: {
            isKeycloak: process.env.USE_KEYCLOAK === "true",
        },
    };
}
