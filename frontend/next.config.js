const withTM = require('next-transpile-modules')([
  '@mui/x-data-grid',
]);

/** @type {import('next').NextConfig} */
const nextConfig = withTM({
  reactStrictMode: false, // speeds up dev + build slightly
  output: 'standalone',
  swcMinify: true,
  eslint: {
    ignoreDuringBuilds: true,
  },
  typescript: {
    ignoreBuildErrors: true,
  },
});

module.exports = nextConfig;
