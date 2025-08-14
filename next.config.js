const withTM = require('next-transpile-modules')([
    '@mui/x-data-grid',
]);

/** @type {import('next').NextConfig} */
const nextConfig = {
    reactStrictMode: true,
    output: 'standalone',
};

module.exports = withTM(nextConfig);
