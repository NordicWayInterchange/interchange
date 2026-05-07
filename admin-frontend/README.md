# ADMIN-FRONTEND

---

## License

---

See full MIT license text [here](license.md).

## Introduction

---

Admin-frontend provides administrators with a comprehensive overview of their own interchange and neighboring interchanges.
The goal is to quickly assess the overall health and status of the system, enabling faster detection of issues and more informed operational decisions. This visual insight helps administrators stay in control, ensure smooth interoperability, and maintain high system availability.


## Technologies

---

- Next.js
- React
- TypeScript
- Leaflet
- Tanstack
- PKI.js
- NextAuth.js
- Keycloak

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

# KeyCloak
USE_KEYCLOAK=
KEYCLOAK_REALM=
KEYCLOAK_CLIENT_ID=
KEYCLOAK_CLIENT_SECRET=
EXTERNAL_KEYCLOAK_URL=
INTERNAL_KEYCLOAK_URL=
```

## Authentication

---

Admin-frontend supports both keycloak and Auth0 as authentication providers.

Keycloak is an open source identity and access management solution. It adds authentication to applications and secure services. https://www.keycloak.org/

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
Keycloak example:
```jsx
providers: [
    Keycloak({
        clientId: process.env.KEYCLOAK_CLIENT_ID,
        clientSecret: process.env.KEYCLOAK_CLIENT_SECRET,
        issuer: process.env.KEYCLOAK_REALM,
    })
  ]
```

## Styles

---

### CSS

To avoid unintended styling we do not use global CSS, and all styling is done at individual components with inline or styled components.

### Theme

We have created two themes for Trafficdata and Interchange portal. They all include a set of shared colors, and their specific colors and fonts. The theme can be changed by importing it and specifying it
in `_app.tsx` , as well as changing the title in `Navbar.tsx`.

```jsx
import { trafficdata, interchangePortal } from "@/theme";

<ThemeProvider theme={trafficdata}>
```

Adjustments to the theme should be performed at `colors.ts` , `fonts.ts` and `trafficdata.ts` / `interchangePortal.ts`.

## Troubleshoot

---

### TypeError: Cannot read properties of undefined (reading 'status')
Most likely related to issues with the PFX:
- Is it valid?
- Is it mounted correctly?
- Is the passphrase correct?