import NextAuth, { DefaultSession } from "next-auth";

declare module "next-auth" {
  interface Session {
    user: {
      commonName: string;
    } & DefaultSession["user"];
  }
}
