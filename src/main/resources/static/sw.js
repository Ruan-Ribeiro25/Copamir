// Service Worker Básico para permitir instalação PWA
self.addEventListener('install', (e) => {
  console.log('[Service Worker] Install');
});

self.addEventListener('fetch', (e) => {
  // Apenas repassa a requisição, não bloqueia nada
  e.respondWith(fetch(e.request));
});