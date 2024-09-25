const path = require('path');
const { override, addWebpackAlias } = require('customize-cra');

module.exports = override(
    addWebpackAlias({
        '@/': path.resolve(__dirname, 'src/'),
        '@/app': path.resolve(__dirname, 'src/app'),
        '@/components': path.resolve(__dirname, 'src/components'),
        '@/hooks': path.resolve(__dirname, 'src/hooks'),
        '@/features': path.resolve(__dirname, 'src/features'),
        '@/assets': path.resolve(__dirname, 'src/assets'),
        '@/types': path.resolve(__dirname, 'src/types'),
        '@/utils': path.resolve(__dirname, 'src/utils'),
        '@/api': path.resolve(__dirname, 'src/api'),
    })
);