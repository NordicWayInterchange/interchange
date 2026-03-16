import Auth0Provider from "next-auth/providers/auth0";
import Keycloak from "next-auth/providers/keycloak";
import {loadAuthConfig} from "./config";

export function buildProviders() {
    const config = loadAuthConfig();
    return config.providers
        .map((p) => {
            if (p.type === "auth0") {
                if (!p.clientId || !p.clientSecret || !p.issuer) return null;
                return Auth0Provider({
                    clientId: p.clientId,
                    clientSecret: p.clientSecret,
                    issuer: p.issuer,
                    authorization: { params: { prompt: "login" } },
                });
            }
            if (p.type === "keycloak") {
                return Keycloak({
                    clientId: p.clientId,
                    clientSecret: p.clientSecret,
                    issuer: `${p.externalUrl}/realms/${p.realm}`,
                    authorization: {
                        url: `${p.externalUrl}/realms/${p.realm}/protocol/openid-connect/auth`,
                        params: { prompt: "login" },
                    },
                    token: `${p.internalUrl}/realms/${p.realm}/protocol/openid-connect/token`,
                    userinfo: `${p.internalUrl}/realms/${p.realm}/protocol/openid-connect/userinfo`,
                });
            }
            return null;
        })
        .filter(Boolean);
}