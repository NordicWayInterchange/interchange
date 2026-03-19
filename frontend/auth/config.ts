import fs from "fs"

import path from "path";
import { z } from "zod";

const CONFIG_PATH = path.join(process.cwd(), "auth.config.json");

export interface AuthProviderConfig {
    type: "keycloak" | "auth0"
    id: string
    name?: string
    clientId: string
    clientSecret: string

    issuer?: string

    realm?: string
    externalUrl?: string
    internalUrl?: string
}

export interface AuthConfig {
    defaultProvider: string
    providers: AuthProviderConfig[]
}

const envSchema = z.object({
    KEYCLOAK_CLIENT_ID: z.string().min(1),
    KEYCLOAK_CLIENT_SECRET: z.string().min(1),
    KEYCLOAK_REALM: z.string().min(1),
    EXTERNAL_KEYCLOAK_URL: z.string(),
    INTERNAL_KEYCLOAK_URL: z.string(),
});

export function loadAuthConfig(): AuthConfig {
    try {
        if (fs.existsSync(CONFIG_PATH)) {
            const raw = fs.readFileSync(CONFIG_PATH, "utf8")
            const config = JSON.parse(raw)
            config.providers = config.providers.map((p: AuthProviderConfig) => ({
                ...p,
                clientId: resolveEnv(p.clientId),
                clientSecret: resolveEnv(p.clientSecret),
                issuer: resolveEnv(p.issuer),
                realm: resolveEnv(p.realm),
                externalUrl: resolveEnv(p.externalUrl),
                internalUrl: resolveEnv(p.internalUrl),
            }))

            return config
        }
    } catch (err) {
        throw new Error("Auth config failed to load");
    }

    const env = envSchema.parse(process.env);

    // Default fallback (Keycloak)
    return {
        defaultProvider: "keycloak",
        providers: [
            {
                type: "keycloak",
                id: "keycloak",
                name: "Login",
                clientId: env.KEYCLOAK_CLIENT_ID,
                clientSecret: env.KEYCLOAK_CLIENT_SECRET,
                realm: env.KEYCLOAK_REALM,
                externalUrl: env.EXTERNAL_KEYCLOAK_URL,
                internalUrl: env.INTERNAL_KEYCLOAK_URL,
            },
        ],
    }
}

function resolveEnv(value?: string) {
    if (!value) return value

    if (value.startsWith("$")) {
        const envName = value.substring(1)
        return process.env[envName]
    }

    return value
}