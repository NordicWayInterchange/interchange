# ADMIN-FRONTEND

---

Admin-frontend provides administrators with a comprehensive overview of their own interchange and neighboring interchanges.
The goal is to quickly assess the overall health and status of the system, enabling faster detection of issues and more informed operational decisions. This visual insight helps administrators stay in control, ensure smooth interoperability, and maintain high system availability.

## Installation and setup

---

### Development

1. Clone the repository and install packages `npm install`
2. Copy environment variables and add PFX file in root. The keys and file are provided in a Keeper vault.
3. Available commands:

```bash
npm run dev # Development mode
npm run watch # Development mode and runs the typeScript compiler (tsc) in watch mode
npm run build # Generate optimized version
npm run start # Start Node.js server
```

```
# Certificate
PFX_KEY_FILENAME=
PFX_PASSPHRASE=

# Interchange
NEXT_PUBLIC_BACKEND_URL=
INTERCHANGE_URI=
NEXT_PUBLIC_INTERCHANGE_PREFIX=

# NextAuth
NEXTAUTH_SECRET=
NEXTAUTH_URL=
SESSION_MAXAGE_SECONDS= # Optional value, will fallback to one day

# Auth0
AUTH0_BASE_URL=
AUTH0_CLIENT_ID=
AUTH0_CLIENT_SECRET=
AUTH0_ISSUER=
```

## Authentication

---

NextAuth.js is an open-source authentication solution for Next.js projects. It has built-in OAuth providers, and for this project, we are using auth0. Users are managed through the auth0 dashboard.

Other providers can be added in […nextAuth].js

```jsx
providers: [
    Auth0Provider({
      clientId: process.env.AUTH0_CLIENT_ID,
      clientSecret: process.env.AUTH0_CLIENT_SECRET,
      issuer: process.env.AUTH0_ISSUER,
    })
  ]
```