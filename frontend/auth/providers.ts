import Auth0Provider from "next-auth/providers/auth0";
import Keycloak from "next-auth/providers/keycloak";

export default function buildProviders() {
    if (process.env.USE_KEYCLOAK) {
        const baseExternal = `${process.env.EXTERNAL_KEYCLOAK_URL}/realms/${process.env.KEYCLOAK_REALM}`;
        const baseInternal = `${process.env.INTERNAL_KEYCLOAK_URL}/realms/${process.env.KEYCLOAK_REALM}`;

        return Keycloak({
            clientId: process.env.KEYCLOAK_CLIENT_ID ?? "clientId is not defined in environment variables",
            clientSecret: process.env.KEYCLOAK_CLIENT_SECRET ?? "clientSecret is not defined in environment variables",
            issuer: baseExternal,
            authorization: {
                url: `${baseExternal}/protocol/openid-connect/auth`,
                params: {prompt: "login"},
            },
            name: `Keycloak (${process.env.KEYCLOAK_REALM})`,
            token: `${baseInternal}/protocol/openid-connect/token`,
            userinfo: `${baseInternal}/protocol/openid-connect/userinfo`,
        })

    } else {
        Auth0Provider({
            clientId: process.env.AUTH0_CLIENT_ID ?? "clientId is not defined in environment variables",
            clientSecret: process.env.AUTH0_CLIENT_SECRET ?? "clientSecret is not defined in environment variables",
            issuer: process.env.AUTH0_ISSUER,
            authorization: {
                params: {
                    prompt: "login",
                },
            },
        })
    }
}