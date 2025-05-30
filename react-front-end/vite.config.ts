import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { VitePWA } from 'vite-plugin-pwa';
import path from 'path';

export default defineConfig({
    plugins: [
        react(),
        VitePWA({
            registerType: 'autoUpdate',
            includeAssets: ['favicon.ico', 'logo192.png'],
            manifest: {
                name: 'Clutter Map',
                short_name: 'ClutterMap',
                start_url: '/',
                display: 'standalone',
                background_color: '#ffffff',
                theme_color: '#000000',
                icons: [
                    {
                        src: '/logo192.png',
                        sizes: '192x192',
                        type: 'image/png'
                    },
                    {
                        src: '/logo512.png',
                        sizes: '512x512',
                        type: 'image/png'
                    }
                ]
            },
            workbox: {
                // Enable SPA routing fallback
                navigateFallback: '/index.html',
                // Runtime caching rules
                runtimeCaching: [
                    {
                        urlPattern: ({ request }) =>
                            request.destination === 'style' || request.destination === 'script',
                        handler: 'StaleWhileRevalidate',
                        options: {
                            cacheName: 'static-resources'
                        }
                    },
                    {
                        urlPattern: ({ request }) => request.destination === 'image',
                        handler: 'CacheFirst',
                        options: {
                            cacheName: 'image-cache',
                            expiration: {
                                maxEntries: 50,
                                maxAgeSeconds: 60 * 24 * 60 * 60 // 60 days
                            }
                        }
                    }
                ]
            }
        })
    ],
    resolve: {
        alias: {
            '@': path.resolve(__dirname, 'src')
        }
    }
});
