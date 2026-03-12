import Keycloak from "next-auth/providers/keycloak"
import Auth0Provider from "next-auth/providers/auth0"
import { loadAuthConfig } from "./config"

export function buildProviders() {
    const config = loadAuthConfig()

    return config.providers.map((p) => {
        if (p.type === "auth0") {
            return Auth0Provider({
                clientId: p.clientId,
                clientSecret: p.clientSecret,
                issuer: p.issuer,
                authorization: {
                    params: {
                        prompt: "login",
                    },
                },
            })
        }
        console.log("KEYCLOAK issuer:",
            `${process.env.EXTERNAL_KEYCLOAK_URL}/realms/${process.env.KEYCLOAK_REALM}`)

        console.log("KEYCLOAK token:",
            `${process.env.INTERNAL_KEYCLOAK_URL}/realms/${process.env.KEYCLOAK_REALM}/protocol/openid-connect/token`)
        if (p.type === "keycloak") {
            return Keycloak({
                clientId: p.clientId,
                clientSecret: p.clientSecret,
                issuer: `${p.externalUrl}/realms/${p.realm}`,
                jwks_endpoint: `${p.internalUrl}/realms/${p.realm}/protocol/openid-connect/certs`,
                authorization: {
                    params: { prompt: "login" },
                    url: `${p.externalUrl}/realms/${p.realm}/protocol/openid-connect/auth`
                },
                token: `${p.internalUrl}/realms/${p.realm}/protocol/openid-connect/token`,
                userinfo: `${p.internalUrl}/realms/${p.realm}/protocol/openid-connect/userinfo`,
            })
        }

        throw new Error(`Unsupported provider: ${p.type}`)
    })
}