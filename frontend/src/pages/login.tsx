import type {
  GetServerSidePropsContext} from "next";
import {signIn} from "next-auth/react";
import { getServerSession } from "next-auth/next";
import { authOptions } from "./api/auth/[...nextauth]";
import Image from "next/image";
import logo from "@/../public/interchange-logo.png";
import { Card, Typography } from "@mui/material";
import * as React from "react";
import { Box } from "@mui/system";
import { StyledButton } from "@/components/shared/styles/StyledSelectorBuilder";

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
      <Box sx={{ alignSelf: "center", mb: -1}}>
        <Image src={logo} alt="Nordic Way logo" width={170} priority={true} />
      </Box>
      <Typography variant="body1">
        Access to this application is restricted to authorized users only. If
        you believe you should have access, please send an email to
        christian.berg.skjetne@vegvesen.no requesting access.
      </Typography>

        <Typography variant="body1">
            Sign in will redirect you to authentication provider.
        </Typography>

        <div>
            {isKeycloak.isKeycloak ? (
                <StyledButton
                    variant="contained"
                    color="buttonThemeColor"
                    sx={{ textTransform: "none", width: 250, alignSelf: "center", ml:10}}
                    onClick={() => signIn("keycloak")}
                >
                    <Typography>Sign in with keycloak</Typography>
                </StyledButton>

            ) : (
                <StyledButton
                    variant="contained"
                    color="buttonThemeColor"
                    sx={{textTransform: "none", width: 250, alignSelf: "center", ml:10}}
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
        return {redirect: {destination: "/"}};
    }
    return {
        props: {
            isKeycloak: process.env.USE_KEYCLOAK === "true",
        },
    };

}
