import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Lock, Clock, ShieldCheck, AlertCircle, X, ArrowRight, CheckCircle2 } from 'lucide-react';
import { CabinClass, PriceFreeze } from '../types/api';
import { pricingService } from '../services/pricingService';
import { useAuth } from '../context/AuthContext';
import { AnimatedPrice } from './AnimatedPrice';
import { modalBackdropVariants, modalDialogVariants } from '../lib/motion';

interface PriceFreezeModalProps {
  flightId: string;
  flightNumber: string;
  cabinClass: CabinClass;
  passengerCount: number;
  currentPrice: number;
  onClose: () => void;
  onFreezeCreated: (freeze: PriceFreeze) => void;
}

export const PriceFreezeModal: React.FC<PriceFreezeModalProps> = ({
  flightId,
  flightNumber,
  cabinClass,
  passengerCount,
  currentPrice,
  onClose,
  onFreezeCreated,
}) => {
  const { isAuthenticated } = useAuth();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [createdFreeze, setCreatedFreeze] = useState<PriceFreeze | null>(null);

  const handleCreateFreeze = async () => {
    if (!isAuthenticated) {
      setError('Please sign in to lock your fare');
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const freeze = await pricingService.createPriceFreeze(
        flightId,
        cabinClass,
        passengerCount
      );
      setCreatedFreeze(freeze);
      onFreezeCreated(freeze);
    } catch (err: any) {
      setError(err.message || 'Failed to create price freeze');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      {/* Backdrop */}
      <motion.div
        variants={modalBackdropVariants}
        initial="hidden"
        animate="visible"
        exit="exit"
        onClick={onClose}
        className="fixed inset-0 bg-black/75 backdrop-blur-md"
      />

      {/* Dialog */}
      <motion.div
        variants={modalDialogVariants}
        initial="hidden"
        animate="visible"
        exit="exit"
        className="relative z-10 bg-slate-900 border border-slate-800 rounded-2xl max-w-md w-full p-6 shadow-2xl"
      >
        <div className="flex items-center justify-between pb-4 border-b border-slate-800">
          <div className="flex items-center gap-2">
            <div className="p-2 bg-slate-800 rounded-lg">
              <Lock className="w-5 h-5 text-amber-400" />
            </div>
            <div>
              <h3 className="text-base font-semibold text-white">Freeze Fare</h3>
              <p className="text-xs text-slate-400">Lock this rate against price surges</p>
            </div>
          </div>
          <motion.button
            whileTap={{ scale: 0.9 }}
            onClick={onClose}
            className="p-1.5 text-slate-400 hover:text-white rounded-lg hover:bg-slate-800 transition-colors"
          >
            <X className="w-5 h-5" />
          </motion.button>
        </div>

        <AnimatePresence>
          {error && (
            <motion.div
              initial={{ opacity: 0, y: -8 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -8 }}
              className="mt-4 p-3 bg-rose-500/10 border border-rose-500/30 text-rose-400 rounded-lg text-xs flex items-center gap-2"
            >
              <AlertCircle className="w-4 h-4 flex-shrink-0" />
              {error}
            </motion.div>
          )}
        </AnimatePresence>

        {createdFreeze ? (
          <motion.div
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            className="mt-6 text-center space-y-4"
          >
            <div className="w-12 h-12 bg-emerald-500/10 border border-emerald-500/30 rounded-xl flex items-center justify-center mx-auto text-emerald-400">
              <CheckCircle2 className="w-6 h-6" />
            </div>
            <div>
              <h4 className="text-base font-semibold text-white">Price Frozen Successfully!</h4>
              <p className="text-xs text-slate-400 mt-1">
                Your fare is locked at{' '}
                <span className="text-white font-semibold">
                  <AnimatedPrice value={createdFreeze.lockedTotalPrice} />
                </span>{' '}
                until{' '}
                <span className="text-white font-medium">
                  {new Date(createdFreeze.expiresAt).toLocaleTimeString([], {
                    hour: '2-digit',
                    minute: '2-digit',
                  })}
                </span>
                .
              </p>
            </div>
            <div className="p-3 bg-slate-800/60 rounded-xl border border-slate-800 text-xs text-slate-300 text-left space-y-1">
              <div className="flex justify-between">
                <span className="text-slate-400">Flight</span>
                <span className="font-semibold text-white">{flightNumber}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-400">Cabin Tier</span>
                <span className="font-semibold text-white">{cabinClass}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-400">Passengers</span>
                <span className="font-semibold text-white">{passengerCount}</span>
              </div>
              <div className="flex justify-between pt-1 border-t border-slate-700">
                <span className="text-slate-400">Locked Price</span>
                <span className="font-bold text-white">
                  <AnimatedPrice value={createdFreeze.lockedTotalPrice} />
                </span>
              </div>
            </div>
            <motion.button
              whileTap={{ scale: 0.97 }}
              onClick={onClose}
              className="w-full py-2.5 bg-gradient-to-r from-amber-400 to-amber-500 hover:from-amber-300 hover:to-amber-400 text-black text-sm font-bold rounded-xl transition shadow-glow-gold"
            >
              Done
            </motion.button>
          </motion.div>
        ) : (
          <div className="mt-4 space-y-4">
            <div className="p-4 bg-slate-800/40 border border-slate-800 rounded-xl space-y-3">
              <div className="flex items-center justify-between text-xs">
                <span className="text-slate-400">Flight:</span>
                <span className="font-semibold text-white">{flightNumber}</span>
              </div>
              <div className="flex items-center justify-between text-xs">
                <span className="text-slate-400">Cabin Class:</span>
                <span className="font-semibold text-white">{cabinClass}</span>
              </div>
              <div className="flex items-center justify-between text-xs">
                <span className="text-slate-400">Passengers:</span>
                <span className="font-semibold text-white">{passengerCount}</span>
              </div>
              <div className="flex items-center justify-between text-xs pt-2 border-t border-slate-700">
                <span className="text-slate-400">Current Total:</span>
                <div className="text-base font-bold text-white">
                  <AnimatedPrice value={currentPrice} />
                </div>
              </div>
            </div>

            <div className="space-y-2 text-xs text-slate-400">
              <div className="flex items-start gap-2">
                <Clock className="w-4 h-4 text-amber-400 flex-shrink-0 mt-0.5" />
                <span>Locks this exact fare for <strong>30 minutes</strong> even if market demand surges.</span>
              </div>
              <div className="flex items-start gap-2">
                <ShieldCheck className="w-4 h-4 text-emerald-400 flex-shrink-0 mt-0.5" />
                <span>No cancellation fee. Automatically applied at checkout when you book.</span>
              </div>
            </div>

            <div className="pt-2 flex items-center justify-end gap-3">
              <motion.button
                whileTap={{ scale: 0.95 }}
                type="button"
                onClick={onClose}
                className="px-4 py-2 text-sm text-slate-400 hover:text-white transition-colors"
              >
                Cancel
              </motion.button>
              <motion.button
                whileTap={{ scale: 0.97 }}
                type="button"
                onClick={handleCreateFreeze}
                disabled={loading}
                className="flex items-center gap-2 px-5 py-2.5 bg-gradient-to-r from-amber-400 to-amber-500 hover:from-amber-300 hover:to-amber-400 disabled:opacity-50 text-black text-sm font-bold rounded-xl transition shadow-glow-gold"
              >
                {loading ? 'Locking...' : 'Lock Price Now'}
                <ArrowRight className="w-4 h-4" />
              </motion.button>
            </div>
          </div>
        )}
      </motion.div>
    </div>
  );
};
