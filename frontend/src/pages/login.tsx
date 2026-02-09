import type {
  GetServerSidePropsContext,
  InferGetServerSidePropsType,
} from "next";
import { signIn } from "next-auth/react";
import { getServerSession } from "next-auth/next";
import { authOptions } from "./api/auth/[...nextauth]";
import Image from "next/image";
import logo from "@/../public/napcore-logo.png";
import { Card, Typography } from "@mui/material";
import * as React from "react";
import { Box } from "@mui/system";
import { StyledButton } from "@/components/shared/styles/StyledSelectorBuilder";

export default function Login({}: InferGetServerSidePropsType<
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
        Sign in will redirect you to our authentication provider.
      </Typography>

      <StyledButton
        variant="contained"
        color={"buttonThemeColor"}
        sx={{ textTransform: "none", width: 200, alignSelf: "center" }}
        onClick={() => {
          /*TODO: get from props*/
          void signIn("auth0");
        }}
      >
        <Typography>Sign in</Typography>
      </StyledButton>
    </Card>
  );
}

export async function getServerSideProps(context: GetServerSidePropsContext) {
  const session = await getServerSession(context.req, context.res, authOptions);

  if (session) {
    return { redirect: { destination: "/" } };
  }

  return {
    props: {},
  };
}
