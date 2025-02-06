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