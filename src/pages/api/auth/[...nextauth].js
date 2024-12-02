import NextAuth from "next-auth";
import Auth0Provider from "next-auth/providers/auth0";
import logger from "../../../lib/logger";

export const authOptions = {
    /**
     * @Description Providers client id/secret
     */
    providers: [
        Auth0Provider({
            clientId: process.env.AUTH0_CLIENT_ID,
            clientSecret: process.env.AUTH0_CLIENT_SECRET,
            issuer: process.env.AUTH0_ISSUER,
            authorization: {
                params: {
                    prompt: "login",
                },
            },
        }),
    ],
    pages: {
        signIn: "/login",
    },
    session: {
        maxAge: parseInt(process.env.SESSION_MAXAGE_SECONDS) || 24 * 60 * 60,
    },
    secret: process.env.NEXTAUTH_SECRET,
    callbacks: {
        async session({ session, token }) {
            session.user.commonName = process.env.INTERCHANGE_PREFIX + token.email;
            return session;
        },
    },
    logger: {
        warn: (message) => console.warn(message),
        debug: (message) => console.debug(message),
        error: (message, error) => console.error(message, error),
    },
    events: {
        async signIn(message) {
            const { email, name } = message.user;
            const { provider, type } = message.account;

            logger.child({ provider, type, name, email }).info("User signed in");
        },
        async signOut(message) {
            const { email, name } = message.token;

            logger.child({ name, email }).info("User signed out");
        },
    },
};
export default NextAuth(authOptions);