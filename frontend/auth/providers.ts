import Auth0Provider from "next-auth/providers/auth0";
import Keycloak from "next-auth/providers/keycloak";
import { loadAuthConfig } from "./config";
import { z } from "zod";


const ProviderSchema = z.object({
    type: z.enum(["auth0", "keycloak"]),
    clientId: z.string().min(1),
    clientSecret: z.string().min(1),

    // auth0
    issuer: z.string().optional(),

    // keycloak
    externalUrl: z.string().optional(),
    internalUrl: z.string().optional(),
    realm: z.string().min(1).optional(),
});

type ProviderConfig = z.infer<typeof ProviderSchema>;

function buildAuth0(p: ProviderConfig) {
    if (!p.issuer || !(p.issuer)) return null;

    return Auth0Provider({
        clientId: p.clientId,
        clientSecret: p.clientSecret,
        issuer: p.issuer,
        name: "Auth0",
        authorization: {
            params: { prompt: "login" },
        },
    });
}

function buildKeycloak(p: ProviderConfig) {
    if (
        !p.externalUrl ||
        !p.internalUrl ||
        !p.realm ||
        !(p.externalUrl) ||
        !(p.internalUrl)
    ) {
        return null;
    }

    const baseExternal = `${p.externalUrl}/realms/${p.realm}`;
    const baseInternal = `${p.internalUrl}/realms/${p.realm}`;

    return Keycloak({
        clientId: p.clientId,
        clientSecret: p.clientSecret,
        issuer: baseExternal,
        authorization: {
            url: `${baseExternal}/protocol/openid-connect/auth`,
            params: { prompt: "login" },
        },
        name: `Keycloak (${p.realm})`,
        token: `${baseInternal}/protocol/openid-connect/token`,
        userinfo: `${baseInternal}/protocol/openid-connect/userinfo`,
    });
}

export default function buildProviders() {
    const config = loadAuthConfig();

    if (!config?.providers || !Array.isArray(config.providers)) {
        return [];
    }

    return config.providers
        .map((raw) => {
            const parsed = ProviderSchema.safeParse(raw);

            if (!parsed.success) {
                console.warn("Invalid provider config skipped:", parsed.error.format());
                return null;
            }

            const p = parsed.data;

            switch (p.type) {
                case "auth0":
                    return buildAuth0(p);

                case "keycloak":
                    return buildKeycloak(p);

                default:
                    return null;
            }
        })
        .filter(Boolean);
}