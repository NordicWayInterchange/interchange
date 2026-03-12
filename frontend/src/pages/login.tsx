import type {
  GetServerSidePropsContext,
  InferGetServerSidePropsType,
} from "next";
import {getProviders, signIn} from "next-auth/react";
import { getServerSession } from "next-auth/next";
import { authOptions } from "./api/auth/[...nextauth]";
import Image from "next/image";
import logo from "@/../public/napcore-logo.png";
import { Card, Typography } from "@mui/material";
import * as React from "react";
import { Box } from "@mui/system";
import { StyledButton } from "@/components/shared/styles/StyledSelectorBuilder";

export default function Login({providers}: InferGetServerSidePropsType<
  typeof getServerSideProps
>) {
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
      <Box sx={{ alignSelf: "center", mb: 3 }}>
        <Image src={logo} alt="Nordic Way logo" width={150} priority={true} />
      </Box>
      <Typography variant="body1">
        Access to this application is restricted to authorized users only. If
        you believe you should have access, please send an email to
        christian.berg.skjetne@vegvesen.no requesting access.
      </Typography>

      <Typography variant="body1">
        Sign in.
      </Typography>

    {providers && Object.values(providers).map((provider) => (
          <div key={provider.id}>
            <StyledButton
                variant="contained"
                color={"buttonThemeColor"}
                sx={{ textTransform: "none", width: 200, alignSelf: "center" }}
                onClick={() => signIn(provider.id)}
            >
              <Typography>Sign in</Typography>
            </StyledButton>
          </div>
      ))}

      {/*<StyledButton*/}
      {/*  variant="contained"*/}
      {/*  color={"buttonThemeColor"}*/}
      {/*  sx={{ textTransform: "none", width: 200, alignSelf: "center" }}*/}
      {/*  onClick={() => {*/}
      {/*    void signIn("auth0");*/}
      {/*  }}*/}
      {/*>*/}
      {/*  <Typography>Sign in</Typography>*/}
      {/*</StyledButton>*/}
    </Card>
  );
}

export async function getServerSideProps(context: GetServerSidePropsContext) {
  const session = await getServerSession(context.req, context.res, authOptions);

  if (session) {
    return { redirect: { destination: "/" } };
  }

  const providers = await getProviders()
  return { props: { providers } }

}
