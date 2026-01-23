const CACHE_NAME = 'vidaplus-v1';
const urlsToCache = [
  '/',
  '/login',
  '/css/home.css', // Adicione outros CSS importantes aqui se quiser
  '/img/logo1.png',
  '/img/logo2.png',
  '/manifest.json'
];

// 1. Instalação: Baixa os arquivos principais para funcionar offline
self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then((cache) => {
        console.log('Cache aberto');
        return cache.addAll(urlsToCache);
      })
  );
});

// 2. Ativação: Limpa caches antigos se você mudar a versão
self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((cacheNames) => {
      return Promise.all(
        cacheNames.map((cacheName) => {
          if (cacheName !== CACHE_NAME) {
            return caches.delete(cacheName);
          }
        })
      );
    })
  );
});

// 3. Fetch (O MAIS IMPORTANTE): Ensina o navegador a buscar no cache ou na rede
// Sem esse trecho abaixo, o botão de instalar NÃO aparece!
self.addEventListener('fetch', (event) => {
  event.respondWith(
    caches.match(event.request)
      .then((response) => {
        // Se achou no cache, retorna o cache. Se não, busca na rede.
        if (response) {
          return response;
        }
        return fetch(event.request);
      })
  );
});