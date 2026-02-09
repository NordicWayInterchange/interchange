const withTM = require('next-transpile-modules')([
  '@mui/x-data-grid',
]);

/** @type {import('next').NextConfig} */
const nextConfig = withTM({
  reactStrictMode: true,
  output: 'standalone',
});

module.exports = nextConfig;
