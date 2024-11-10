/* eslint-disable no-restricted-globals */

const CACHE_NAME = 'clutter-map-cache-v1';
const urlsToCache = [
    '/',
    '/index.html',
    '/manifest.json',
    '/favicon.ico',
    '/logo192.png',
    '/logo512.png',
];

// Install event - cache resources
self.addEventListener('install', event => {
    event.waitUntil(
        caches.open(CACHE_NAME)
            .then(cache => cache.addAll(urlsToCache))
            .then(() => self.skipWaiting())
            .catch(error => {
                console.error('Failed to cache during install:', error);
            })
    );
});

self.addEventListener('fetch', event => {
    event.respondWith(
        caches.match(event.request).then(response => {
            if (response) {
                return response; // Serve from cache if available
            }

            return fetch(event.request).then(networkResponse => {
                // Dynamically cache files in the 'static' folder only
                if (event.request.url.includes('/static/')) {
                    return caches.open(CACHE_NAME).then(cache => {
                        console.log('Caching dynamically:', event.request.url); // Log cached URLs
                        cache.put(event.request, networkResponse.clone());
                        return networkResponse; // Return after caching
                    });
                }
                return networkResponse; // Return network response if not caching
            }).catch(() => {
                // If offline and not cached, show fallback for navigation requests
                if (event.request.mode === 'navigate') {
                    return caches.match('/index.html'); // Or provide a custom offline page
                }
            });
        })
    );
});

self.addEventListener('activate', event => {
    event.waitUntil(
        caches.keys().then(cacheNames => {
            return Promise.all(
                cacheNames.map(cacheName => {
                    if (cacheName !== CACHE_NAME) {
                        return caches.delete(cacheName);
                    }
                })
            );
        })
    );
    self.clients.claim();
});