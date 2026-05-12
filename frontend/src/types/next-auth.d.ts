import NextAuth, { DefaultSession } from "next-auth";

declare module "next-auth" {
  interface Session {
    user: {
      commonName: string;
      /** Present when the user belongs to an Organization. The organization's bound ServiceProvider name. */
      organization?: string;
    } & DefaultSession["user"];
  }
}
