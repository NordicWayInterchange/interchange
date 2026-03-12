import fs from "fs"

const CONFIG_PATH = "/etc/napcore/auth.config.json"

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
            return JSON.parse(raw)
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