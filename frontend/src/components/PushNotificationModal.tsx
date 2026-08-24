import React, { useState, useEffect } from 'react';
import {
  Bell,
  BellRing,
  BellOff,
  CheckCircle2,
  Smartphone,
  Monitor,
  Apple,
  Globe,
  Share,
  PlusSquare,
  ShieldCheck,
  Send,
  X,
  RefreshCw,
  Info
} from 'lucide-react';
import { pushNotificationService, PlatformInfo } from '../services/pushNotificationService';
import { useAuth } from '../context/AuthContext';

interface PushNotificationModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const PushNotificationModal: React.FC<PushNotificationModalProps> = ({
  isOpen,
  onClose,
}) => {
  const { isAuthenticated } = useAuth();
  const [platform, setPlatform] = useState<PlatformInfo | null>(null);
  const [isSubscribed, setIsSubscribed] = useState(false);
  const [loading, setLoading] = useState(false);
  const [testLoading, setTestLoading] = useState(false);
  const [statusMessage, setStatusMessage] = useState<{ type: 'success' | 'error' | 'info'; text: string } | null>(null);

  const checkStatus = async () => {
    const info = pushNotificationService.getPlatformInfo();
    setPlatform(info);
    if (info.isPushSupported) {
      const subscribed = await pushNotificationService.isSubscribed();
      setIsSubscribed(subscribed);
    }
  };

  useEffect(() => {
    if (isOpen) {
      checkStatus();
      setStatusMessage(null);
    }
  }, [isOpen]);

  if (!isOpen || !platform) return null;

  const handleTogglePush = async () => {
    if (!isAuthenticated) {
      setStatusMessage({
        type: 'error',
        text: 'Please sign in to register push notifications to your account.',
      });
      return;
    }

    setLoading(true);
    setStatusMessage(null);

    try {
      if (isSubscribed) {
        await pushNotificationService.unsubscribe();
        setIsSubscribed(false);
        setStatusMessage({
          type: 'info',
          text: 'Push notifications have been disabled for this device.',
        });
      } else {
        const success = await pushNotificationService.subscribe();
        if (success) {
          setIsSubscribed(true);
          setStatusMessage({
            type: 'success',
            text: `Push notifications enabled for ${platform.browser} on ${platform.os}!`,
          });
        } else {
          setStatusMessage({
            type: 'error',
            text: 'Notification permission was dismissed or denied in browser settings.',
          });
        }
      }
    } catch (err: any) {
      setStatusMessage({
        type: 'error',
        text: err.message || 'Failed to update push notification subscription.',
      });
    } finally {
      setLoading(false);
    }
  };

  const handleSendTestPush = async () => {
    if (!isAuthenticated) {
      setStatusMessage({
        type: 'error',
        text: 'Please sign in to send test push notifications.',
      });
      return;
    }

    setTestLoading(true);
    setStatusMessage(null);

    try {
      await pushNotificationService.sendTestPush();
      setStatusMessage({
        type: 'success',
        text: 'Test push notification dispatched! Check your device notification center / screen.',
      });
    } catch (err: any) {
      setStatusMessage({
        type: 'error',
        text: err.message || 'Failed to trigger test push notification.',
      });
    } finally {
      setTestLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-md animate-fade-in">
      <div className="relative w-full max-w-lg rounded-2xl bg-slate-900 border border-slate-800 shadow-2xl overflow-hidden">
        {/* Header */}
        <div className="p-6 border-b border-slate-800 bg-gradient-to-r from-slate-950 via-slate-900 to-slate-950 flex items-start justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-blue-500/10 border border-blue-500/20 flex items-center justify-center text-blue-400">
              <BellRing className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-base font-bold text-white flex items-center gap-2">
                Push Notifications
                <span className="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                  Universal Multi-Platform
                </span>
              </h3>
              <p className="text-xs text-slate-400 mt-0.5">
                Real-time alerts for flight delays, gate changes, and bookings
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition"
            aria-label="Close"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content Body */}
        <div className="p-6 space-y-5 max-h-[75vh] overflow-y-auto">
          {/* Current Device & Browser Info Badge */}
          <div className="p-3.5 rounded-xl bg-slate-950/80 border border-slate-800 flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="p-2 rounded-lg bg-slate-800/80 text-blue-400">
                {platform.os === 'iOS' ? (
                  <Apple className="w-5 h-5" />
                ) : platform.os === 'Android' ? (
                  <Smartphone className="w-5 h-5" />
                ) : platform.os === 'Windows' || platform.os === 'macOS' ? (
                  <Monitor className="w-5 h-5" />
                ) : (
                  <Globe className="w-5 h-5" />
                )}
              </div>
              <div>
                <p className="text-xs font-semibold text-white">
                  {platform.browser} on {platform.os}
                  {platform.isStandalone && ' (PWA Installed)'}
                </p>
                <p className="text-[11px] text-slate-400">
                  Permission: <span className="font-mono text-slate-300 capitalize">{platform.permission}</span>
                </p>
              </div>
            </div>

            <div className="flex items-center gap-1.5">
              {isSubscribed ? (
                <span className="flex items-center gap-1 px-2.5 py-1 rounded-lg text-xs font-medium bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                  <CheckCircle2 className="w-3.5 h-3.5" />
                  Active
                </span>
              ) : (
                <span className="flex items-center gap-1 px-2.5 py-1 rounded-lg text-xs font-medium bg-slate-800 text-slate-400 border border-slate-700">
                  <BellOff className="w-3.5 h-3.5" />
                  Disabled
                </span>
              )}
            </div>
          </div>

          {/* iOS Specific Instructions (if not standalone PWA) */}
          {platform.requiresHomeScreenPWA && (
            <div className="p-4 rounded-xl bg-amber-500/10 border border-amber-500/30 text-amber-200 text-xs space-y-2.5">
              <div className="flex items-center gap-2 font-semibold text-amber-300">
                <Apple className="w-4 h-4" />
                <span>iOS Safari Web Push Setup Required</span>
              </div>
              <p className="text-[11px] leading-relaxed text-amber-200/90">
                Apple requires web push to be enabled through an installed Home Screen app on iOS 16.4+:
              </p>
              <ol className="list-decimal list-inside space-y-1 text-[11px] text-amber-100/90 pl-1 font-medium">
                <li>
                  Tap the Safari <span className="inline-flex items-center font-bold text-white bg-slate-800 px-1.5 py-0.5 rounded mx-1"><Share className="w-3 h-3 inline mr-1" /> Share</span> button
                </li>
                <li>
                  Scroll down and select <span className="inline-flex items-center font-bold text-white bg-slate-800 px-1.5 py-0.5 rounded mx-1"><PlusSquare className="w-3 h-3 inline mr-1" /> Add to Home Screen</span>
                </li>
                <li>Launch <strong>SmartTravel</strong> from your Home Screen and tap Enable Alerts</li>
              </ol>
            </div>
          )}

          {/* Status Feedback Message */}
          {statusMessage && (
            <div
              className={`p-3.5 rounded-xl border text-xs flex items-start gap-2.5 animate-fade-in ${
                statusMessage.type === 'success'
                  ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-300'
                  : statusMessage.type === 'error'
                  ? 'bg-rose-500/10 border-rose-500/30 text-rose-300'
                  : 'bg-blue-500/10 border-blue-500/30 text-blue-300'
              }`}
            >
              <Info className="w-4 h-4 shrink-0 mt-0.5" />
              <span>{statusMessage.text}</span>
            </div>
          )}

          {/* Action Buttons */}
          <div className="space-y-3">
            <button
              onClick={handleTogglePush}
              disabled={loading || platform.requiresHomeScreenPWA}
              className={`w-full py-3 px-4 rounded-xl text-xs sm:text-sm font-semibold transition flex items-center justify-center gap-2 shadow-lg disabled:opacity-50 disabled:cursor-not-allowed ${
                isSubscribed
                  ? 'bg-slate-800 hover:bg-slate-700 text-rose-300 border border-slate-700 hover:border-rose-500/30'
                  : 'bg-blue-600 hover:bg-blue-500 text-white shadow-blue-500/20'
              }`}
            >
              {loading ? (
                <>
                  <RefreshCw className="w-4 h-4 animate-spin" />
                  <span>Configuring Push Registration...</span>
                </>
              ) : isSubscribed ? (
                <>
                  <BellOff className="w-4 h-4" />
                  <span>Disable Notifications on this Device</span>
                </>
              ) : (
                <>
                  <Bell className="w-4 h-4" />
                  <span>Enable Push Notifications</span>
                </>
              )}
            </button>

            {isSubscribed && (
              <button
                onClick={handleSendTestPush}
                disabled={testLoading}
                className="w-full py-2.5 px-4 rounded-xl text-xs font-medium bg-slate-950 hover:bg-slate-850 text-slate-300 hover:text-white border border-slate-800 transition flex items-center justify-center gap-2 disabled:opacity-50"
              >
                {testLoading ? (
                  <>
                    <RefreshCw className="w-3.5 h-3.5 animate-spin text-blue-400" />
                    <span>Dispatching Test Push...</span>
                  </>
                ) : (
                  <>
                    <Send className="w-3.5 h-3.5 text-blue-400" />
                    <span>Send Test Notification Alert</span>
                  </>
                )}
              </button>
            )}
          </div>

          {/* Supported Platforms Grid */}
          <div className="pt-3 border-t border-slate-800/80">
            <p className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider mb-2.5">
              Supported Platforms & Browsers
            </p>
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
              {[
                { name: 'iOS Safari', desc: 'iOS 16.4+ PWA', icon: Apple },
                { name: 'Android', desc: 'Chrome & Edge', icon: Smartphone },
                { name: 'Windows', desc: 'Edge / Chrome / FF', icon: Monitor },
                { name: 'macOS Safari', desc: 'Safari 16+ VAPID', icon: Globe },
              ].map((p, idx) => {
                const IconComponent = p.icon;
                return (
                  <div
                    key={idx}
                    className="p-2.5 rounded-lg bg-slate-950/60 border border-slate-800/80 flex flex-col items-center text-center gap-1"
                  >
                    <IconComponent className="w-4 h-4 text-blue-400" />
                    <span className="text-[11px] font-medium text-slate-200">{p.name}</span>
                    <span className="text-[9px] text-slate-500">{p.desc}</span>
                  </div>
                );
              })}
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="p-4 border-t border-slate-800 bg-slate-950 text-center">
          <p className="text-[10px] text-slate-500 flex items-center justify-center gap-1.5">
            <ShieldCheck className="w-3.5 h-3.5 text-emerald-400" />
            <span>Encrypted RFC 8291/8292 VAPID Protocol & IDOR-Protected</span>
          </p>
        </div>
      </div>
    </div>
  );
};
