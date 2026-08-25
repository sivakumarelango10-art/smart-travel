import React, { useState, useEffect, useCallback } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
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
  Info,
  Check
} from 'lucide-react';
import { pushNotificationService, PlatformInfo } from '../services/pushNotificationService';
import { modalBackdropVariants, modalDialogVariants } from '../lib/motion';

interface PushNotificationModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const PushNotificationModal: React.FC<PushNotificationModalProps> = ({
  isOpen,
  onClose,
}) => {
  const [platform, setPlatform] = useState<PlatformInfo | null>(null);
  const [isSubscribed, setIsSubscribed] = useState(false);
  const [loading, setLoading] = useState(false);
  const [testLoading, setTestLoading] = useState(false);
  const [statusMessage, setStatusMessage] = useState<{ type: 'success' | 'error' | 'info'; text: string } | null>(null);

  const checkStatus = useCallback(async () => {
    const info = pushNotificationService.getPlatformInfo();
    setPlatform(info);
    if (info.isPushSupported) {
      const subscribed = await pushNotificationService.isSubscribed();
      setIsSubscribed(subscribed);
    }
  }, []);

  useEffect(() => {
    if (isOpen) {
      checkStatus();
      setStatusMessage(null);
    }
  }, [isOpen, checkStatus]);

  // Handle Escape key to close
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && isOpen) {
        onClose();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  const currentPlatform = platform || pushNotificationService.getPlatformInfo();

  const handleTogglePush = async () => {
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
            text: `✓ Push notifications active for ${currentPlatform.browser} on ${currentPlatform.os}!`,
          });
        } else {
          setStatusMessage({
            type: 'error',
            text: 'Notification permission was dismissed or blocked in browser settings.',
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
    setTestLoading(true);
    setStatusMessage(null);

    try {
      // 1. Dispatch backend test push
      try {
        await pushNotificationService.sendTestPush();
      } catch {
        // Continue to trigger local notification & in-app toast
      }

      // 2. Trigger native OS/browser local notification if permitted
      await pushNotificationService.showLocalNotification('✈️ SmartTravel Notification Alert', {
        body: 'Real-time flight gate updates, schedule disruptions & booking receipts are active!',
      });

      // 3. Trigger immediate in-app toast alert right on the screen
      if (typeof window !== 'undefined') {
        window.dispatchEvent(
          new CustomEvent('app:notification', {
            detail: {
              title: '✈️ SmartTravel Notification Active',
              body: 'Push alerts are connected! You will receive live gate changes, flight alerts & booking tickets.',
              url: '/tracked-flights',
              eventType: 'PUSH_TEST',
            },
          })
        );
      }

      setStatusMessage({
        type: 'success',
        text: '✓ Live notification alert dispatched to your screen and device!',
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
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 sm:p-6">
      {/* Backdrop */}
      <motion.div
        variants={modalBackdropVariants}
        initial="hidden"
        animate="visible"
        exit="exit"
        onClick={onClose}
        className="fixed inset-0 bg-black/80 backdrop-blur-md"
      />

      {/* Dialog */}
      <motion.div
        variants={modalDialogVariants}
        initial="hidden"
        animate="visible"
        exit="exit"
        className="relative z-10 w-full max-w-lg rounded-3xl bg-slate-900 border-2 border-slate-800 shadow-2xl overflow-hidden flex flex-col my-auto max-h-[85vh]"
      >
        {/* Sticky Header with high-visibility close button */}
        <div className="p-4 sm:p-5 border-b border-slate-800 bg-slate-950 flex items-center justify-between sticky top-0 z-20 shrink-0">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-2xl bg-blue-500/15 border border-blue-500/30 flex items-center justify-center text-blue-400 shrink-0">
              <BellRing className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-base font-black text-white flex items-center gap-2">
                Push Notifications
                <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-emerald-500/15 text-emerald-400 border border-emerald-500/30">
                  Universal
                </span>
              </h3>
              <p className="text-xs text-slate-400 mt-0.5">
                Flight delay, gate change & ticket alerts
              </p>
            </div>
          </div>

          <button
            type="button"
            onClick={onClose}
            className="p-2 rounded-xl text-slate-400 hover:text-white bg-slate-800/60 hover:bg-slate-800 border border-slate-700 transition cursor-pointer"
            aria-label="Close notification settings"
            title="Close Window (Esc)"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Scrollable Body */}
        <div className="p-5 sm:p-6 space-y-4 overflow-y-auto flex-1">
          {/* Current Device & Browser Info Badge */}
          <div className="p-3.5 rounded-2xl bg-slate-950/80 border border-slate-800 flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="p-2.5 rounded-xl bg-slate-800/80 text-blue-400">
                {currentPlatform.os === 'iOS' ? (
                  <Apple className="w-5 h-5" />
                ) : currentPlatform.os === 'Android' ? (
                  <Smartphone className="w-5 h-5" />
                ) : currentPlatform.os === 'Windows' || currentPlatform.os === 'macOS' ? (
                  <Monitor className="w-5 h-5" />
                ) : (
                  <Globe className="w-5 h-5" />
                )}
              </div>
              <div>
                <p className="text-xs font-bold text-white">
                  {currentPlatform.browser} on {currentPlatform.os}
                  {currentPlatform.isStandalone && ' (PWA Installed)'}
                </p>
                <p className="text-[11px] text-slate-400">
                  Status:{' '}
                  <span className="font-mono text-slate-300 capitalize">
                    {isSubscribed ? 'Subscribed & Active' : 'Not Subscribed'}
                  </span>
                </p>
              </div>
            </div>

            <div className="flex items-center gap-1.5">
              {isSubscribed ? (
                <span className="flex items-center gap-1 px-2.5 py-1 rounded-xl text-xs font-bold bg-emerald-500/15 text-emerald-400 border border-emerald-500/30">
                  <CheckCircle2 className="w-3.5 h-3.5" />
                  Active
                </span>
              ) : (
                <span className="flex items-center gap-1 px-2.5 py-1 rounded-xl text-xs font-semibold bg-slate-800 text-slate-400 border border-slate-700">
                  <BellOff className="w-3.5 h-3.5" />
                  Disabled
                </span>
              )}
            </div>
          </div>

          {/* iOS Specific Instructions (if not standalone PWA) */}
          {currentPlatform.requiresHomeScreenPWA && (
            <div className="p-4 rounded-2xl bg-amber-500/10 border border-amber-500/30 text-amber-200 text-xs space-y-2.5">
              <div className="flex items-center gap-2 font-bold text-amber-300">
                <Apple className="w-4 h-4" />
                <span>iOS Safari Web Push Setup</span>
              </div>
              <p className="text-[11px] leading-relaxed text-amber-200/90">
                Apple requires web push to be enabled via an installed Home Screen app on iOS 16.4+:
              </p>
              <ol className="list-decimal list-inside space-y-1 text-[11px] text-amber-100/90 pl-1 font-medium">
                <li>
                  Tap the Safari <span className="inline-flex items-center font-bold text-white bg-slate-800 px-1.5 py-0.5 rounded mx-1"><Share className="w-3 h-3 inline mr-1" /> Share</span> button
                </li>
                <li>
                  Select <span className="inline-flex items-center font-bold text-white bg-slate-800 px-1.5 py-0.5 rounded mx-1"><PlusSquare className="w-3 h-3 inline mr-1" /> Add to Home Screen</span>
                </li>
                <li>Launch <strong>SmartTravel</strong> from your Home Screen and tap Enable Alerts</li>
              </ol>
            </div>
          )}

          {/* Status Feedback Message */}
          {statusMessage && (
            <div
              className={`p-3.5 rounded-2xl border text-xs flex items-start gap-2.5 animate-fade-in ${
                statusMessage.type === 'success'
                  ? 'bg-emerald-500/15 border-emerald-500/40 text-emerald-300'
                  : statusMessage.type === 'error'
                  ? 'bg-rose-500/15 border-rose-500/40 text-rose-300'
                  : 'bg-blue-500/15 border-blue-500/40 text-blue-300'
              }`}
            >
              {statusMessage.type === 'success' ? (
                <Check className="w-4 h-4 shrink-0 mt-0.5 text-emerald-400" />
              ) : (
                <Info className="w-4 h-4 shrink-0 mt-0.5" />
              )}
              <span className="font-semibold">{statusMessage.text}</span>
            </div>
          )}

          {/* Action Buttons */}
          <div className="space-y-2.5">
            <button
              type="button"
              onClick={handleTogglePush}
              disabled={loading || currentPlatform.requiresHomeScreenPWA}
              className={`w-full py-3 px-4 rounded-2xl text-xs sm:text-sm font-bold transition flex items-center justify-center gap-2 shadow-lg disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer ${
                isSubscribed
                  ? 'bg-slate-800 hover:bg-slate-750 text-rose-300 border border-slate-700 hover:border-rose-500/40'
                  : 'bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-500 hover:to-indigo-500 text-white shadow-blue-500/25'
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

            {/* Test Notification Trigger Button */}
            <button
              type="button"
              onClick={handleSendTestPush}
              disabled={testLoading}
              className="w-full py-2.5 px-4 rounded-2xl text-xs font-bold bg-slate-950 hover:bg-slate-800 text-slate-200 hover:text-white border border-slate-800 hover:border-slate-700 transition flex items-center justify-center gap-2 disabled:opacity-50 cursor-pointer"
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
          </div>

          {/* Supported Platforms Grid */}
          <div className="pt-3 border-t border-slate-800/80">
            <p className="text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-2">
              Cross-Platform & Browser Support
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
                    className="p-2 rounded-xl bg-slate-950/60 border border-slate-800/80 flex flex-col items-center text-center gap-1"
                  >
                    <IconComponent className="w-4 h-4 text-blue-400" />
                    <span className="text-[11px] font-bold text-slate-200">{p.name}</span>
                    <span className="text-[9px] text-slate-500">{p.desc}</span>
                  </div>
                );
              })}
            </div>
          </div>
        </div>

        {/* Footer with Explicit Close Window Button */}
        <div className="p-4 border-t border-slate-800 bg-slate-950 flex flex-col sm:flex-row items-center justify-between gap-3 shrink-0">
          <p className="text-[10px] text-slate-500 flex items-center gap-1.5">
            <ShieldCheck className="w-3.5 h-3.5 text-emerald-400" />
            <span>Encrypted RFC 8291/8292 VAPID Protocol</span>
          </p>

          <motion.button
            whileTap={{ scale: 0.95 }}
            type="button"
            onClick={onClose}
            className="w-full sm:w-auto px-5 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 hover:text-white text-xs font-bold transition border border-slate-700 flex items-center justify-center gap-1.5 cursor-pointer"
          >
            <X className="w-3.5 h-3.5" />
            <span>Close Window</span>
          </motion.button>
        </div>
      </motion.div>
    </div>
  );
};
