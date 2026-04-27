const withTM = require('next-transpile-modules')([
  '@mui/x-data-grid',
]);

/** @type {import('next').NextConfig} */
const nextConfig = withTM({
  reactStrictMode: false, // speeds up dev + build slightly
  output: 'standalone'
});

module.exports = nextConfig;
