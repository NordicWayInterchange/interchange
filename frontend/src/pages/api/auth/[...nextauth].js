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
        const orgs = profile.organization;
        if (orgs && typeof orgs === 'object' && !Array.isArray(orgs)) {
          // Object format (Add organization attributes enabled):
          // { "alias": { "name": "Display Name", ... } }
          const alias = Object.keys(orgs)[0] ?? null;
          const attrs = alias ? orgs[alias] : null;
          token.organization = alias;
          token.organizationName = attrs?.name?.[0] ?? attrs?.displayName?.[0] ?? alias;
        } else if (Array.isArray(orgs)) {
          // Fallback: array format (attributes not enabled)
          token.organization = orgs[0] ?? null;
          token.organizationName = orgs[0] ?? null;
        } else {
          token.organization = null;
          token.organizationName = null;
        }
      }
      return token;
    },
    async session({ session, token }) {
      session.user.commonName = token.organization
        ? escapeString(process.env.INTERCHANGE_PREFIX + token.organization.toLowerCase())
        : escapeString(process.env.INTERCHANGE_PREFIX + token.email);

      if (token.organization) {
        session.user.organization = String(token.organizationName ?? token.organization);
      }

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
