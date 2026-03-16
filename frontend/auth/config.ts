import fs from "fs"

const CONFIG_PATH = "/frontend/auth.config.json"

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
        console.error("Failed to load auth config:", err)
    }

    // Default fallback (Keycloak)
    return {
        defaultProvider: "keycloak",
        providers: [
            {
                type: "keycloak",
                id: "keycloak",
                name: "Login",
                clientId: process.env.KEYCLOAK_CLIENT_ID!,
                clientSecret: process.env.KEYCLOAK_CLIENT_SECRET!,
                realm: process.env.KEYCLOAK_REALM!,
                externalUrl: process.env.EXTERNAL_KEYCLOAK_URL!,
                internalUrl: process.env.INTERNAL_KEYCLOAK_URL!,
            }
        ]
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