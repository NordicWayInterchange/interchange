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
      // At sign-in, profile is populated with claims from the identity provider.
      // Forward the custom org/SP claims onto the persisted token so they survive
      // across requests (profile is only present on the initial sign-in call).
      if (profile) {
        // DEBUG: log raw profile claims to identify correct Keycloak mapper output names.
        logger.child({ profile }).debug("Raw IdP profile claims at sign-in");
        // Placeholder claim names — replace with the actual Keycloak mapper output names.
        token.organization = Array.isArray(profile.organization)
          ? profile.organization[0] ?? null
          : null;
      }
      return token;
    },
    async session({ session, token }) {
      // A user that belongs to an Organization has a commonName claim set to the
      // org's bound ServiceProvider name. Users without an org (or using Auth0)
      // fall back to the email-derived name.
      session.user.commonName = token.organization
        ? escapeString(process.env.INTERCHANGE_PREFIX + token.organization.toLowerCase())
        : escapeString(process.env.INTERCHANGE_PREFIX + token.email);

      if (token.organization) {
        session.user.organization = String(token.organization);
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
