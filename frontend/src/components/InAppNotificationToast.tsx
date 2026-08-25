import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Bell, X, ExternalLink, AlertCircle, Clock } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { toastVariants } from '../lib/motion';

export interface ToastNotification {
  id: string;
  title: string;
  body: string;
  url?: string;
  eventType?: string;
}

export const InAppNotificationToast: React.FC = () => {
  const navigate = useNavigate();
  const [activeToast, setActiveToast] = useState<ToastNotification | null>(null);

  useEffect(() => {
    // 1. Listen for Service Worker Broadcasts
    const handleServiceWorkerMessage = (event: MessageEvent) => {
      if (event.data && event.data.type === 'SMARTTRAVEL_PUSH_RECEIVED') {
        const p = event.data.payload;
        setActiveToast({
          id: 'sw-' + Date.now(),
          title: p.title || 'SmartTravel Alert',
          body: p.body || '',
          url: p.url || '/tracked-flights',
          eventType: p.eventType,
        });
      }
    };

    // 2. Listen for Direct App Custom Events (e.g. test pushes or real-time web socket events)
    const handleCustomAppNotification = (event: any) => {
      const p = event.detail || event;
      if (p) {
        setActiveToast({
          id: 'app-' + Date.now(),
          title: p.title || 'SmartTravel Alert',
          body: p.body || '',
          url: p.url || '/tracked-flights',
          eventType: p.eventType || 'TEST_PUSH',
        });
      }
    };

    if (typeof window !== 'undefined') {
      window.addEventListener('app:notification', handleCustomAppNotification);
      window.addEventListener('sw:push', handleCustomAppNotification);
      if ('serviceWorker' in navigator) {
        navigator.serviceWorker.addEventListener('message', handleServiceWorkerMessage);
      }
    }

    return () => {
      if (typeof window !== 'undefined') {
        window.removeEventListener('app:notification', handleCustomAppNotification);
        window.removeEventListener('sw:push', handleCustomAppNotification);
        if ('serviceWorker' in navigator) {
          navigator.serviceWorker.removeEventListener('message', handleServiceWorkerMessage);
        }
      }
    };
  }, []);

  // Auto-dismiss after 7 seconds
  useEffect(() => {
    if (activeToast) {
      const timer = setTimeout(() => {
        setActiveToast(null);
      }, 7000);
      return () => clearTimeout(timer);
    }
  }, [activeToast]);

  const handleClick = () => {
    if (!activeToast) return;
    const target = activeToast.url || '/tracked-flights';
    setActiveToast(null);
    navigate(target);
  };

  return (
    <div className="fixed bottom-5 right-5 z-50 max-w-sm w-full pointer-events-none">
      <AnimatePresence>
        {activeToast && (
          <motion.div
            key={activeToast.id}
            variants={toastVariants}
            initial="hidden"
            animate="visible"
            exit="exit"
            className="pointer-events-auto"
          >
            <div
              onClick={handleClick}
              className="cursor-pointer p-4 rounded-2xl bg-slate-900/95 border-2 border-amber-400/50 shadow-2xl backdrop-blur-lg hover:border-amber-400 transition duration-200 group"
            >
              <div className="flex items-start gap-3">
                <div className="p-2 rounded-xl bg-amber-400/15 border border-amber-400/30 text-amber-400 shrink-0 group-hover:scale-105 transition">
                  {activeToast.eventType?.includes('CANCEL') ? (
                    <AlertCircle className="w-5 h-5 text-rose-400" />
                  ) : activeToast.eventType?.includes('DELAY') ? (
                    <Clock className="w-5 h-5 text-amber-400" />
                  ) : (
                    <Bell className="w-5 h-5 text-amber-400" />
                  )}
                </div>

                <div className="flex-1 min-w-0">
                  <div className="flex items-center justify-between gap-1">
                    <h4 className="text-xs font-bold text-white truncate">
                      {activeToast.title}
                    </h4>
                    <motion.button
                      whileTap={{ scale: 0.88 }}
                      type="button"
                      onClick={(e) => {
                        e.stopPropagation();
                        setActiveToast(null);
                      }}
                      className="text-slate-400 hover:text-white p-1 rounded-lg hover:bg-slate-800 transition"
                      aria-label="Dismiss toast"
                    >
                      <X className="w-3.5 h-3.5" />
                    </motion.button>
                  </div>
                  <p className="text-[11px] text-slate-300 mt-0.5 line-clamp-2 leading-relaxed">
                    {activeToast.body}
                  </p>
                  <div className="mt-2 flex items-center gap-1 text-[10px] font-semibold text-amber-400 group-hover:text-amber-300">
                    <span>View Details</span>
                    <ExternalLink className="w-3 h-3" />
                  </div>
                </div>
              </div>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
};
