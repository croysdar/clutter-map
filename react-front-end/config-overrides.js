const path = require('path');
const { override, addWebpackAlias } = require('customize-cra');

module.exports = override(
    addWebpackAlias({
        '@/': path.resolve(__dirname, 'src/'),
        '@/api': path.resolve(__dirname, 'src/api'),
        '@/app': path.resolve(__dirname, 'src/app'),
        '@/assets': path.resolve(__dirname, 'src/assets'),
        '@/components': path.resolve(__dirname, 'src/components'),
        '@/features': path.resolve(__dirname, 'src/features'),
        '@/hooks': path.resolve(__dirname, 'src/hooks'),
        '@/pages': path.resolve(__dirname, 'src/pages'),
        '@/types': path.resolve(__dirname, 'src/types'),
        '@/utils': path.resolve(__dirname, 'src/utils'),
    })
);