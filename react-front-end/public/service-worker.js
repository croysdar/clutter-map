/* eslint-disable no-restricted-globals */

/* global workbox importScripts */

// Load Workbox from the CDN
importScripts('https://storage.googleapis.com/workbox-cdn/releases/6.1.5/workbox-sw.js');

if (workbox) {
    console.log('Workbox is loaded');

    workbox.core.clientsClaim();

    // Manually add index.html to the precache
    workbox.precaching.precacheAndRoute([{ url: '/index.html', revision: null }]);

    // Precache and route assets listed in the precache manifest
    workbox.precaching.precacheAndRoute(self.__WB_MANIFEST || []);

    // Use a handler to serve index.html for all navigation requests, enabling SPA routing
    const handler = workbox.precaching.createHandlerBoundToURL('/index.html');
    const navigationRoute = new workbox.routing.NavigationRoute(handler);
    workbox.routing.registerRoute(navigationRoute);

    // Cache CSS and JS files with Stale-While-Revalidate strategy
    workbox.routing.registerRoute(
        ({ request }) => request.destination === 'style' || request.destination === 'script',
        new workbox.strategies.StaleWhileRevalidate({
            cacheName: 'static-resources',
        })
    );

    // Cache image assets with a Cache First strategy
    workbox.routing.registerRoute(
        ({ request }) => request.destination === 'image',
        new workbox.strategies.CacheFirst({
            cacheName: 'image-cache',
            plugins: [
                new workbox.expiration.ExpirationPlugin({
                    maxEntries: 50,
                    maxAgeSeconds: 30 * 24 * 60 * 60, // Cache images for 30 days
                }),
            ],
        })
    );

    console.log('Service worker registered with Workbox');
} else {
    console.log('Workbox could not be loaded');
}