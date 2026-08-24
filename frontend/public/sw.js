// SmartTravel Production Service Worker (PWA & Web Push Notification Engine)
// Compatible with iOS (PWA standalone), Android, Windows, macOS Safari, Chrome, Firefox, Edge, Opera, Brave

const SW_VERSION = 'smarttravel-sw-v2.0.0';

// 1. Lifecycle: Instant Installation
self.addEventListener('install', function (event) {
  self.skipWaiting();
});

// 2. Lifecycle: Immediate Activation & Control
self.addEventListener('activate', function (event) {
  event.waitUntil(self.clients.claim());
});

// 3. Push Event: Multi-Platform Push Notification Receiver
self.addEventListener('push', function (event) {
  let payload = {
    title: 'SmartTravel Alert',
    body: 'You have a new flight or travel update.',
    icon: '/logo.png',
    badge: '/logo.png',
    url: '/tracked-flights',
    tag: 'smarttravel-general-alert',
    eventType: 'GENERAL_UPDATE',
    timestamp: Date.now(),
  };

  if (event.data) {
    try {
      const data = event.data.json();
      payload = {
        title: data.title || payload.title,
        body: data.body || payload.body,
        icon: data.icon || payload.icon,
        badge: data.badge || payload.badge,
        url: data.url || payload.url,
        tag: data.tag || payload.tag,
        eventType: data.eventType || payload.eventType,
        timestamp: data.timestamp || Date.now(),
      };
    } catch (err) {
      payload.body = event.data.text();
    }
  }

  // Cross-Platform Vibration Pattern: [vibrate, pause, vibrate]
  const vibrationPattern = [200, 100, 200, 100, 200];

  const notificationOptions = {
    body: payload.body,
    icon: payload.icon || '/logo.png',
    badge: payload.badge || '/logo.png',
    tag: payload.tag,
    data: {
      url: payload.url,
      eventType: payload.eventType,
      timestamp: payload.timestamp,
    },
    requireInteraction: true,
    renotify: true,
    vibrate: vibrationPattern,
    actions: [
      { action: 'open_url', title: '✈️ View Update' },
      { action: 'dismiss', title: 'Dismiss' },
    ],
  };

  // Broadcast to open client windows for in-app live sync
  const broadcastPromise = self.clients
    .matchAll({ type: 'window', includeUncontrolled: true })
    .then(function (clientList) {
      for (let i = 0; i < clientList.length; i++) {
        clientList[i].postMessage({
          type: 'SMARTTRAVEL_PUSH_RECEIVED',
          payload: payload,
        });
      }
    });

  // Display native OS push notification
  const showNotificationPromise = self.registration.showNotification(
    payload.title,
    notificationOptions
  );

  event.waitUntil(Promise.all([showNotificationPromise, broadcastPromise]));
});

// 4. Notification Click: Deep Link & Action Router
self.addEventListener('notificationclick', function (event) {
  event.notification.close();

  if (event.action === 'dismiss') {
    return;
  }

  const targetUrl =
    (event.notification.data && event.notification.data.url) ||
    '/tracked-flights';

  event.waitUntil(
    self.clients
      .matchAll({ type: 'window', includeUncontrolled: true })
      .then(function (clientList) {
        // If a window with the app is open, focus it and navigate
        for (let i = 0; i < clientList.length; i++) {
          const client = clientList[i];
          if ('focus' in client) {
            if (client.url.includes(targetUrl)) {
              return client.focus();
            } else {
              client.navigate(targetUrl);
              return client.focus();
            }
          }
        }
        // Otherwise open a new window
        if (self.clients.openWindow) {
          return self.clients.openWindow(targetUrl);
        }
      })
  );
});

// 5. Message Event: Direct Client-to-SW communication
self.addEventListener('message', function (event) {
  if (event.data && event.data.type === 'SKIP_WAITING') {
    self.skipWaiting();
  }
});
