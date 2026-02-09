export { default } from "next-auth/middleware";

export const config = {
  /**
   * @Description Secure certain pages
   */
  matcher: ["/((?!api|login|_next/static|_next/image|favicon.ico).*)"],
};
