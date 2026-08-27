if (typeof (window as any).global === 'undefined') {
  (window as any).global = window;
}

import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import './index.css';
import { pushNotificationService } from './services/pushNotificationService';

// Initialize Service Worker for Web Push & PWA
if (typeof window !== 'undefined' && 'serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    pushNotificationService.registerServiceWorker().catch((err) => {
      console.warn('Initial Service Worker registration skipped:', err);
    });
  });
}

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
);

