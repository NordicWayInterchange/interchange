import NextAuth from "next-auth";
import { escapeString } from "@/lib/escapeString";
import buildProviders from "../../../../auth/providers";
const logger = require("../../../lib/logger");

export const authOptions = {
  /**
   * @Description Providers client id/secret
   */
  providers: [buildProviders()],

  session: {
    maxAge: parseInt(process.env.SESSION_MAXAGE_SECONDS) || 24 * 60 * 60,
  },
  secret: process.env.NEXTAUTH_SECRET,
  callbacks: {
    async jwt({ token, profile }) {
      if (profile) {
        logger.child({ profile }).debug("Profile claims at sign-in");
        token.organization = Array.isArray(profile.organization)
          ? profile.organization[0] ?? null
          : null;
      }
      return token;
    },
    async session({ session, token }) {
      session.user.commonName = token.organization
        ? escapeString(process.env.INTERCHANGE_PREFIX + token.organization.toLowerCase())
        : escapeString(process.env.INTERCHANGE_PREFIX + token.email);

      if (token.organization) {
        session.user.organization = String(token.organization);
      }

      session.user.readOnly = session.user.organization != null;

      return session;
    },
  },
  events: {
    async signIn(message) {
      const { email, name } = message.user;
      const { provider, type } = message.account;

      logger.child({ provider, type, name, email }).info("User logged in");
    },
    async signOut(message) {
      const { email, name } = message.token;

      logger.child({ name, email }).info("User logged out");
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
  pages: {
    signIn: "/login",
  },
};
export default NextAuth(authOptions);
