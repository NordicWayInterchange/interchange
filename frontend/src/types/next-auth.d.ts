import { DefaultSession } from "next-auth";

declare module "next-auth" {
  interface Session {
    user: {
      commonName: string;
      /* Present when the user belongs to an Organization in keycloak. The organization's bound ServiceProvider name. */
      organization?: string;
      readOnly: boolean;
      email?: string;
    } & DefaultSession["user"];
  }
}
