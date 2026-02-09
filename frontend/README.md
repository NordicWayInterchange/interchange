# NAPCORE

---

## License

---

See full MIT license text [here](license.md).

## Introduction

---

This is a self-help portal for interchange end-users to manage capabilities, subscriptions, deliveries and private channels.

![architecture drawio (1)](https://github.com/NordicWayInterchange/napcore-frontend/assets/127298906/88333580-19a0-4404-bb15-28c50d0304f5)

## Technologies

---

- Next.js
- React
- TypeScript
- Leaflet
- Tanstack
- PKI.js
- NextAuth.js

## Installation and setup

---

### Development

1. Clone the repository and install packages `npm install`
2. Copy environment variables and add PFX file in root.
3. Available commands:

```bash
npm run dev # Development mode
npm run build # Generate optimized version
npm run start # Start Node.js server
```

```
# Certificate
PFX_KEY_FILENAME=
PFX_PASSPHRASE=

# Interchange
INTERCHANGE_URI=
NEXT_PUBLIC_INTERCHANGE_PREFIX=

# NextAuth
NEXTAUTH_SECRET=
NEXTAUTH_URL=
SESSION_MAXAGE_SECONDS= # Optional value, will fallback to one day

# Auth0
AUTH0_CLIENT_ID=
AUTH0_CLIENT_SECRET=
AUTH0_ISSUER=
```

### Docker

1. Start Docker.
2. Create and run a shell script with the following content.

```bash
#!/bin/bash -eu

docker run \
              -p 3000:3000 \
              -v <PATH_FILE>:/app/<FILENAME> \
              -e PFX_KEY_FILENAME=<FILENAME> \
              -e PFX_PASSPHRASE=<PASSPHRASE> \
              -e INTERCHANGE_URI=<URI> \
              -e INTERCHANGE_PREFIX=<PREFIX> \
              -e NEXTAUTH_SECRET=<SECRET> \
              -e AUTH0_CLIENT_ID=<CLIENT_ID> \
              -e AUTH0_CLIENT_SECRET=<CLIENT_SECRET> \
              -e AUTH0_ISSUER=<ISSUER> \
              -e NEXTAUTH_URL=<URL> \
              -e SESSION_MAXAGE_SECONDS=<SECONDS> \ # Optional value, will fallback to one day
              europe-west4-docker.pkg.dev/nw-shared-w3ml/nordic-way-interchange/napcore-frontend:1.0
```

## Certificate signing request

---

We use the JavaScript library PKI.js to create a CSR. The CSR is created client-side, Base64 encoded, and then sent as a request to the server. The server responds with the signed client certificate and CA certificates as Base64 encoded PEM files.

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

### Callbacks

Whenever a session is checked we add a commonName value to the session object, which prefixes the email with the interchange prefix.
This allows us to send the prefix as an environment variable, instead of bundling it in the build.

```jsx
callbacks: {
    async session({ session, token }) {
      session.user.commonName = process.env.INTERCHANGE_PREFIX + token.email;

      return session;
    }
  }
```

### Middleware

The middleware.ts allows us to run code before a request is completed. With NextAuth we can export a config object with a regex matcher, to specify allowed routings for an unauthenticated user.

Additionally, we check in the backend (for frontend), that all of these criteria are met:

- Does the user have a valid token?
- Does the user have a valid session?
- Does the authenticated user equal the user on the request?

If so, continue with the request or respond with an HTTP 403.

## Styles

---

### CSS

To avoid unintended styling we do not use global CSS, and all styling is done at individual components with inline or styled components.

### Theme

We have created two themes for Trafficdata and Transportportal. They all include a set of shared colors, and their specific colors and fonts. The theme can be changed by importing it and specifying it
in `_app.tsx` , as well as changing the title in `Navbar.tsx`.

```jsx
import { trafficdata, transportportal } from "@/theme";

<ThemeProvider theme={trafficdata}>
```

Adjustments to the theme should be performed at `colors.ts` , `fonts.ts` and `trafficdata.ts` / `transportportal.ts`.

## Troubleshoot

---

### TypeError: Cannot read properties of undefined (reading 'status')
Most likely related to issues with the PFX:
- Is it valid?
- Is it mounted correctly?
- Is the passphrase correct?