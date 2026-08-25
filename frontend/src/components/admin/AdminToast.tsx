import React, { createContext, useContext, useState, useCallback } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { CheckCircle, XCircle, AlertTriangle, Info, X } from 'lucide-react';
import { toastVariants } from '../../lib/motion';

type ToastType = 'success' | 'error' | 'warning' | 'info';

interface Toast {
  id: string;
  type: ToastType;
  title: string;
  message?: string;
}

interface ToastContextType {
  showToast: (type: ToastType, title: string, message?: string) => void;
}

const ToastContext = createContext<ToastContextType | undefined>(undefined);

export const AdminToastProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [toasts, setToasts] = useState<Toast[]>([]);

  const showToast = useCallback((type: ToastType, title: string, message?: string) => {
    const id = Math.random().toString(36).slice(2);
    setToasts(prev => [...prev, { id, type, title, message }]);
    setTimeout(() => {
      setToasts(prev => prev.filter(t => t.id !== id));
    }, 4500);
  }, []);

  const removeToast = (id: string) => setToasts(prev => prev.filter(t => t.id !== id));

  const icons: Record<ToastType, React.ReactNode> = {
    success: <CheckCircle className="w-4 h-4 text-emerald-400" />,
    error:   <XCircle className="w-4 h-4 text-rose-400" />,
    warning: <AlertTriangle className="w-4 h-4 text-amber-400" />,
    info:    <Info className="w-4 h-4 text-sky-400" />,
  };

  const borderClasses: Record<ToastType, string> = {
    success: 'border-l-emerald-500',
    error:   'border-l-rose-500',
    warning: 'border-l-amber-500',
    info:    'border-l-sky-500',
  };

  return (
    <ToastContext.Provider value={{ showToast }}>
      {children}
      <div className="fixed bottom-4 right-4 z-[100] flex flex-col gap-2 max-w-sm pointer-events-none">
        <AnimatePresence>
          {toasts.map(toast => (
            <motion.div
              key={toast.id}
              variants={toastVariants}
              initial="hidden"
              animate="visible"
              exit="exit"
              className="pointer-events-auto"
            >
              <div
                className={`flex items-start gap-3 p-4 bg-slate-900 border border-slate-700 border-l-4 ${borderClasses[toast.type]} rounded-xl shadow-2xl`}
              >
                <div className="flex-shrink-0 mt-0.5">{icons[toast.type]}</div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-semibold text-white">{toast.title}</p>
                  {toast.message && <p className="text-xs text-slate-400 mt-0.5">{toast.message}</p>}
                </div>
                <motion.button
                  whileTap={{ scale: 0.9 }}
                  onClick={() => removeToast(toast.id)}
                  className="text-slate-500 hover:text-white transition p-0.5 rounded-lg hover:bg-slate-800"
                >
                  <X className="w-4 h-4" />
                </motion.button>
              </div>
            </motion.div>
          ))}
        </AnimatePresence>
      </div>
    </ToastContext.Provider>
  );
};

export const useAdminToast = (): ToastContextType => {
  const ctx = useContext(ToastContext);
  if (!ctx) throw new Error('useAdminToast must be used within AdminToastProvider');
  return ctx;
};
