import React, { useState, useEffect } from 'react';
import { WifiOff, RefreshCw, CheckCircle2 } from 'lucide-react';

export const NetworkStatusBanner: React.FC = () => {
  const [isOnline, setIsOnline] = useState(navigator.onLine);
  const [showReconnected, setShowReconnected] = useState(false);

  useEffect(() => {
    const handleOnline = () => {
      setIsOnline(true);
      setShowReconnected(true);
      const timer = setTimeout(() => setShowReconnected(false), 3500);
      return () => clearTimeout(timer);
    };

    const handleOffline = () => {
      setIsOnline(false);
      setShowReconnected(false);
    };

    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);

    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
    };
  }, []);

  if (isOnline && !showReconnected) {
    return null;
  }

  return (
    <div
      role="alert"
      aria-live="assertive"
      className={`fixed bottom-4 right-4 z-50 px-4 py-3 rounded-2xl shadow-2xl border flex items-center gap-3 text-xs font-semibold backdrop-blur-xl transition-all duration-300 ${
        isOnline
          ? 'bg-emerald-950/90 text-emerald-300 border-emerald-500/30'
          : 'bg-rose-950/90 text-rose-300 border-rose-500/30 animate-pulse'
      }`}
    >
      {isOnline ? (
        <>
          <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
          <span>Connection restored. Back online!</span>
        </>
      ) : (
        <>
          <WifiOff className="w-4 h-4 text-rose-400 shrink-0" />
          <span>You are currently offline. Real-time updates paused.</span>
          <button
            type="button"
            onClick={() => window.location.reload()}
            className="ml-2 px-2.5 py-1 rounded-lg bg-rose-500/20 hover:bg-rose-500/30 text-rose-200 text-[11px] font-bold border border-rose-500/30 transition flex items-center gap-1"
          >
            <RefreshCw className="w-3 h-3" />
            Retry
          </button>
        </>
      )}
    </div>
  );
};
