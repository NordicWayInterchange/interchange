import NextAuth from "next-auth";
import logger from "../../../lib/logger";
import buildProviders from "../../../../auth/providers";

export const authOptions = {
    /**
     * @Description Providers client id/secret
     */
    secret: process.env.NEXTAUTH_SECRET,
    session: {
        maxAge: parseInt(process.env.SESSION_MAXAGE_SECONDS) || 24 * 60 * 60,
    },

    providers: [buildProviders()],

    debug: true,
    pages: {
        signIn: "/login",
    },
    jwt: {
        encryption: true,
    },
    callbacks: {
        async session({ session, token }) {
            session.user.commonName = process.env.INTERCHANGE_PREFIX + token.email;
            return session;
        },
    },
    logger: {
        error(code, metadata) {
            logger.error({ code, metadata });
        },
        warn(code) {
            logger.warn({ code });
        },
        debug(code) {
            logger.debug({ code });
        },
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