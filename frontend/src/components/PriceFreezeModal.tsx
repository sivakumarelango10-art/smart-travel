import React, { useState } from 'react';
import { Lock, Clock, ShieldCheck, AlertCircle, X, ArrowRight, CheckCircle2 } from 'lucide-react';
import { CabinClass, PriceFreeze } from '../types/api';
import { pricingService } from '../services/pricingService';
import { useAuth } from '../context/AuthContext';

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
    <div className="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-md w-full p-6 shadow-2xl animate-in fade-in zoom-in duration-150">
        <div className="flex items-center justify-between pb-4 border-b border-slate-800">
          <div className="flex items-center gap-2">
            <div className="p-2 bg-cyan-500/10 border border-cyan-500/20 rounded-xl">
              <Lock className="w-5 h-5 text-cyan-400" />
            </div>
            <div>
              <h3 className="text-base font-bold text-white">Freeze Fare</h3>
              <p className="text-xs text-slate-400">Lock this rate against price surges</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 text-slate-400 hover:text-white rounded-lg hover:bg-slate-800 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {error && (
          <div className="mt-4 p-3 bg-rose-500/10 border border-rose-500/30 text-rose-400 rounded-xl text-xs flex items-center gap-2">
            <AlertCircle className="w-4 h-4 flex-shrink-0" />
            {error}
          </div>
        )}

        {createdFreeze ? (
          <div className="mt-6 text-center space-y-4">
            <div className="w-12 h-12 bg-emerald-500/10 border border-emerald-500/30 rounded-2xl flex items-center justify-center mx-auto text-emerald-400">
              <CheckCircle2 className="w-6 h-6" />
            </div>
            <div>
              <h4 className="text-base font-bold text-white">Price Frozen Successfully!</h4>
              <p className="text-xs text-slate-400 mt-1">
                Your fare is locked at{' '}
                <span className="text-cyan-400 font-semibold">
                  ₹{createdFreeze.lockedTotalPrice.toLocaleString()}
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
                <span className="font-bold text-cyan-400">₹{createdFreeze.lockedTotalPrice.toLocaleString()}</span>
              </div>
            </div>
            <button
              onClick={onClose}
              className="w-full py-2.5 bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 text-white text-sm font-semibold rounded-xl transition-all shadow-lg shadow-cyan-500/20"
            >
              Done
            </button>
          </div>
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
                <span className="text-base font-bold text-cyan-400">₹{currentPrice.toLocaleString()}</span>
              </div>
            </div>

            <div className="space-y-2 text-xs text-slate-400">
              <div className="flex items-start gap-2">
                <Clock className="w-4 h-4 text-cyan-400 flex-shrink-0 mt-0.5" />
                <span>Locks this exact fare for <strong>30 minutes</strong> even if market demand surges.</span>
              </div>
              <div className="flex items-start gap-2">
                <ShieldCheck className="w-4 h-4 text-emerald-400 flex-shrink-0 mt-0.5" />
                <span>No cancellation fee. Automatically applied at checkout when you book.</span>
              </div>
            </div>

            <div className="pt-2 flex items-center justify-end gap-3">
              <button
                type="button"
                onClick={onClose}
                className="px-4 py-2 text-sm text-slate-400 hover:text-white transition-colors"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={handleCreateFreeze}
                disabled={loading}
                className="flex items-center gap-2 px-5 py-2.5 bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 disabled:opacity-50 text-white text-sm font-semibold rounded-xl transition-all shadow-lg shadow-cyan-500/20"
              >
                {loading ? 'Locking...' : 'Lock Price Now'}
                <ArrowRight className="w-4 h-4" />
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
